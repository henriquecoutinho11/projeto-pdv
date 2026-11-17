package com.restaurante.pdv.model.service;

import com.restaurante.pdv.dto.request.ConfirmacaoPagamentoDTO;
import com.restaurante.pdv.dto.request.SimulacaoCartaoDTO;
import com.restaurante.pdv.dto.response.CobrancaPixResponseDTO;
import com.restaurante.pdv.dto.response.PagamentoResponseDTO;
import com.restaurante.pdv.exception.NegocioException;
import com.restaurante.pdv.exception.RecursoNaoEncontradoException;
import com.restaurante.pdv.model.entity.CobrancaPix;
import com.restaurante.pdv.model.entity.FormaPagamento;
import com.restaurante.pdv.model.entity.Pedido;
import com.restaurante.pdv.model.entity.StatusPedido;
import com.restaurante.pdv.model.repository.CobrancaPixRepository;
import com.restaurante.pdv.model.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço de processamento de pagamentos para totens e PDV.
 * Suporta emissão de QR Code PIX (com expiração em 5 minutos) e simulação de terminal de cartão.
 */
@Service
@Transactional(readOnly = true)
public class PagamentoService {

    private final PedidoRepository pedidoRepository;
    private final CobrancaPixRepository cobrancaPixRepository;
    private final PixEmvService pixEmvService;

    @Value("${pdv.pagamento.pix.chave:financeiro@antuneslanches.com.br}")
    private String chavePix;

    @Value("${pdv.pagamento.pix.nome-recebedor:Antunes Lanches}")
    private String nomeRecebedor;

    @Value("${pdv.pagamento.pix.cidade-recebedor:Sao Paulo}")
    private String cidadeRecebedor;

    @Value("${pdv.pagamento.pix.expiracao-minutos:5}")
    private int tempoExpiracaoMinutos;

    public PagamentoService(PedidoRepository pedidoRepository,
                            CobrancaPixRepository cobrancaPixRepository,
                            PixEmvService pixEmvService) {
        this.pedidoRepository = pedidoRepository;
        this.cobrancaPixRepository = cobrancaPixRepository;
        this.pixEmvService = pixEmvService;
    }

    /**
     * Gera ou recupera a cobrança instantânea PIX ativa para determinado pedido.
     * Produz o payload EMV copia-e-cola e o QR Code em Base64 com janela de validade de 5 minutos.
     *
     * @param pedidoId identificador do pedido.
     * @return DTO contendo payload, imagem QR Code e temporizador de expiração.
     */
    @Transactional
    public CobrancaPixResponseDTO gerarCobrancaPix(Long pedidoId) {
        Pedido pedido = buscarPedido(pedidoId);

        if (pedido.isPago()) {
            throw new NegocioException(String.format("O pedido #%d já se encontra pago.", pedidoId));
        }

        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new NegocioException(String.format("Não é possível gerar cobrança PIX para o pedido cancelado #%d.", pedidoId));
        }

        if (pedido.getValorTotal() == null || pedido.getValorTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("O valor do pedido deve ser maior que zero para geração de cobrança PIX.");
        }

        // Reutilizar cobrança ativa recente se ainda não expirou
        Optional<CobrancaPix> cobrancaExistente = cobrancaPixRepository.findTopByPedidoIdOrderByDataCriacaoDesc(pedidoId);
        if (cobrancaExistente.isPresent() && cobrancaExistente.get().isAtiva()) {
            return CobrancaPixResponseDTO.deEntidade(cobrancaExistente.get());
        }

        // Criar identificador único de transação (txId alfanumérico máx. 25 chars)
        String sufixoUnico = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        String txId = "AL" + pedidoId + sufixoUnico;
        if (txId.length() > 25) {
            txId = txId.substring(0, 25);
        }

        String payload = pixEmvService.gerarPayload(chavePix, nomeRecebedor, cidadeRecebedor, txId, pedido.getValorTotal());
        String qrCodeBase64 = pixEmvService.gerarQrCodeBase64(payload, 300, 300);

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime expiracao = agora.plusMinutes(tempoExpiracaoMinutos);

        CobrancaPix novaCobranca = new CobrancaPix(
                pedido,
                txId,
                chavePix,
                pedido.getValorTotal(),
                payload,
                qrCodeBase64,
                agora,
                expiracao
        );

        CobrancaPix cobrancaSalva = cobrancaPixRepository.save(novaCobranca);
        return CobrancaPixResponseDTO.deEntidade(cobrancaSalva);
    }

    /**
     * Valida o recebimento e confirma o pagamento de um pedido.
     * Atualiza o pedido como pago e registra os metadados da transação.
     *
     * @param pedidoId identificador do pedido.
     * @param dto dados do pagamento enviado pelo Totem ou webhook bancário.
     * @return DTO com o resultado da confirmação de pagamento.
     */
    @Transactional
    public PagamentoResponseDTO confirmarPagamento(Long pedidoId, ConfirmacaoPagamentoDTO dto) {
        if (dto == null) {
            throw new NegocioException("Os dados de confirmação de pagamento são obrigatórios.");
        }

        if (dto.getFormaPagamento() == null) {
            throw new NegocioException("Forma de pagamento inválida.");
        }

        Pedido pedido = buscarPedido(pedidoId);

        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new NegocioException(String.format("Não é possível pagar o pedido cancelado #%d.", pedidoId));
        }

        if (pedido.isPago()) {
            throw new NegocioException(String.format("O pedido #%d já está pago.", pedidoId));
        }

        if (dto.getValorPago() == null || dto.getValorPago().compareTo(pedido.getValorTotal()) != 0) {
            throw new NegocioException(String.format(
                    "Valor pago (R$ %s) difere do valor total do pedido (R$ %s).",
                    dto.getValorPago() != null ? dto.getValorPago() : BigDecimal.ZERO,
                    pedido.getValorTotal()
            ));
        }

        // Validações específicas para fluxo PIX
        if (dto.getFormaPagamento() == FormaPagamento.PIX) {
            CobrancaPix cobrancaPix = cobrancaPixRepository.findTopByPedidoIdOrderByDataCriacaoDesc(pedidoId)
                    .orElseThrow(() -> new NegocioException("Nenhuma cobrança PIX encontrada para este pedido."));

            if (cobrancaPix.isExpirada()) {
                throw new NegocioException("Cobrança PIX expirada. Gere uma nova cobrança para efetuar o pagamento.");
            }

            cobrancaPix.setPago(Boolean.TRUE);
            cobrancaPix.setDataHoraPagamento(LocalDateTime.now());
            cobrancaPixRepository.save(cobrancaPix);
        }

        LocalDateTime agora = LocalDateTime.now();
        String codigoTransacao = (dto.getCodigoTransacao() != null && !dto.getCodigoTransacao().isBlank())
                ? dto.getCodigoTransacao()
                : gerarCodigoTransacao(dto.getFormaPagamento(), pedidoId);

        pedido.setPago(Boolean.TRUE);
        pedido.setDataHoraPagamento(agora);
        pedido.setFormaPagamento(dto.getFormaPagamento());
        pedido.setCodigoTransacao(codigoTransacao);

        Pedido pedidoAtualizado = pedidoRepository.save(pedido);

        return PagamentoResponseDTO.dePedido(pedidoAtualizado, "Pagamento confirmado com sucesso!");
    }

    /**
     * Consulta a cobrança PIX gerada mais recentemente para o pedido.
     */
    public CobrancaPixResponseDTO consultarCobrancaPix(Long pedidoId) {
        buscarPedido(pedidoId);
        CobrancaPix cobranca = cobrancaPixRepository.findTopByPedidoIdOrderByDataCriacaoDesc(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        String.format("Nenhuma cobrança PIX encontrada para o pedido #%d", pedidoId)
                ));
        return CobrancaPixResponseDTO.deEntidade(cobranca);
    }

    /**
     * Consulta o status de pagamento de um pedido (usado para polling no Totem).
     */
    public PagamentoResponseDTO consultarStatusPagamento(Long pedidoId) {
        Pedido pedido = buscarPedido(pedidoId);
        String mensagem = pedido.isPago() ? "Pagamento efetuado com sucesso." : "Aguardando pagamento.";
        return PagamentoResponseDTO.dePedido(pedido, mensagem);
    }

    /**
     * Simula a aprovação de uma transação no terminal de cartão touch do Totem (Crédito ou Débito).
     */
    @Transactional
    public PagamentoResponseDTO simularPagamentoCartao(Long pedidoId, SimulacaoCartaoDTO dto) {
        if (dto == null || dto.getFormaPagamento() == null) {
            throw new NegocioException("Forma de pagamento inválida. É obrigatório informar a modalidade do cartão.");
        }

        if (!dto.getFormaPagamento().isCartao()) {
            throw new NegocioException("A modalidade deve ser CARTAO_CREDITO ou CARTAO_DEBITO.");
        }

        Pedido pedido = buscarPedido(pedidoId);

        String nsu = "NSU-" + System.currentTimeMillis() % 100000000;
        ConfirmacaoPagamentoDTO confirmacao = new ConfirmacaoPagamentoDTO(dto.getFormaPagamento(), pedido.getValorTotal(), nsu);
        confirmacao.setBandeira(dto.getBandeira() != null ? dto.getBandeira() : "MASTERCARD");
        confirmacao.setNsu(nsu);

        return confirmarPagamento(pedidoId, confirmacao);
    }

    private Pedido buscarPedido(Long pedidoId) {
        if (pedidoId == null) {
            throw new NegocioException("O ID do pedido é obrigatório.");
        }
        return pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", pedidoId));
    }

    private String gerarCodigoTransacao(FormaPagamento formaPagamento, Long pedidoId) {
        String prefixo = switch (formaPagamento) {
            case PIX -> "PIX";
            case CARTAO_CREDITO -> "CC";
            case CARTAO_DEBITO -> "CD";
            case DINHEIRO -> "DIN";
        };
        return String.format("%s-%d-%d", prefixo, pedidoId, System.currentTimeMillis() % 1000000);
    }
}
