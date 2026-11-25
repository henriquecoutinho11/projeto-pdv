package com.restaurante.pdv.model.service;

import com.restaurante.pdv.dto.request.CustomizacaoItemDTO;
import com.restaurante.pdv.dto.request.NovoPedidoDTO;
import com.restaurante.pdv.dto.response.ItemPedidoResponseDTO;
import com.restaurante.pdv.dto.response.PedidoKdsResponseDTO;
import com.restaurante.pdv.dto.response.PedidoResponseDTO;
import com.restaurante.pdv.exception.NegocioException;
import com.restaurante.pdv.exception.RecursoNaoEncontradoException;
import com.restaurante.pdv.exception.TransicaoStatusInvalidaException;
import com.restaurante.pdv.model.entity.*;
import com.restaurante.pdv.model.repository.OpcaoAdicionalRepository;
import com.restaurante.pdv.model.repository.PedidoRepository;
import com.restaurante.pdv.model.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.restaurante.pdv.event.PedidoCriadoEvent;
import com.restaurante.pdv.event.PedidoStatusAlteradoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import com.restaurante.pdv.validation.CpfValidoValidator;

/**
 * Serviço responsável pelas regras de negócio de pedidos e motor de customização de lanches/combos (McDonald's Style).
 */
@Service
@Transactional(readOnly = true)
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final OpcaoAdicionalRepository opcaoAdicionalRepository;
    private final SenhaSequencialService senhaSequencialService;
    private final ApplicationEventPublisher eventPublisher;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProdutoRepository produtoRepository,
                         OpcaoAdicionalRepository opcaoAdicionalRepository) {
        this(pedidoRepository, produtoRepository, opcaoAdicionalRepository, new SenhaSequencialService(pedidoRepository), null);
    }

    public PedidoService(PedidoRepository pedidoRepository,
                         ProdutoRepository produtoRepository,
                         OpcaoAdicionalRepository opcaoAdicionalRepository,
                         SenhaSequencialService senhaSequencialService) {
        this(pedidoRepository, produtoRepository, opcaoAdicionalRepository, senhaSequencialService, null);
    }

    @Autowired
    public PedidoService(PedidoRepository pedidoRepository,
                         ProdutoRepository produtoRepository,
                         OpcaoAdicionalRepository opcaoAdicionalRepository,
                         SenhaSequencialService senhaSequencialService,
                         ApplicationEventPublisher eventPublisher) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.opcaoAdicionalRepository = opcaoAdicionalRepository;
        this.senhaSequencialService = senhaSequencialService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Processa atomicamente o fechamento de um novo pedido no PDV/Totem.
     * Valida os itens, a integridade dos produtos e o estoque disponível, debita as quantidades em estoque,
     * recalcula os valores estritamente no servidor e persiste o pedido e seus itens associados.
     *
     * @param dto dados de submissão do pedido contendo a lista de itens customizados.
     * @return DTO detalhado com os dados completos do pedido persistido com status inicial RECEBIDO.
     */
    @Transactional
    public PedidoResponseDTO processarNovoPedido(NovoPedidoDTO dto) {
        if (dto == null) {
            throw new NegocioException("Os dados do pedido são obrigatórios.");
        }

        if (dto.getItens() == null || dto.getItens().isEmpty()) {
            throw new NegocioException("O pedido deve conter pelo menos um item.");
        }

        if (dto.getCpfCliente() != null && !dto.getCpfCliente().isBlank()) {
            if (!CpfValidoValidator.isCpfValido(dto.getCpfCliente())) {
                throw new NegocioException("O CPF informado é inválido.");
            }
        }

        Pedido pedido = new Pedido();
        pedido.setStatus(StatusPedido.RECEBIDO);
        pedido.setDataHora(LocalDateTime.now());
        pedido.setFormaPagamento(dto.getFormaPagamento());
        pedido.setNomeCliente(dto.getNomeCliente());

        String cpfFormatado = CpfValidoValidator.formatarCpf(dto.getCpfCliente());
        pedido.setCpfCliente(cpfFormatado != null ? cpfFormatado : dto.getCpfCliente());

        // Atribuir número de senha sequencial diária
        Integer numeroSenha = senhaSequencialService.gerarProximaSenha();
        pedido.setNumeroSenha(numeroSenha);

        Map<Long, Integer> quantidadesPorProduto = new HashMap<>();

        for (CustomizacaoItemDTO itemDto : dto.getItens()) {
            ItemPedido item = montarItemPedido(itemDto);
            pedido.adicionarItem(item);

            Produto produto = item.getProduto();
            Long produtoId = produto.getId();
            int quantidadeSolicitada = item.getQuantidade();
            quantidadesPorProduto.merge(produtoId, quantidadeSolicitada, Integer::sum);
        }

        // Valida o estoque agregado e efetua toda a baixa antes de persistir o pedido.
        for (Map.Entry<Long, Integer> entrada : quantidadesPorProduto.entrySet()) {
            Produto produto = produtoRepository.findById(entrada.getKey())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", entrada.getKey()));

            int quantidadeSolicitada = entrada.getValue();
            Integer estoqueAtual = produto.getQuantidadeEstoque();

            if (estoqueAtual == null || estoqueAtual < quantidadeSolicitada) {
                throw new NegocioException(String.format(
                        "Estoque insuficiente para o produto '%s'. Disponível: %d, Solicitado: %d.",
                        produto.getNome(),
                        estoqueAtual != null ? estoqueAtual : 0,
                        quantidadeSolicitada
                ));
            }

            produto.setQuantidadeEstoque(estoqueAtual - quantidadeSolicitada);
            produtoRepository.save(produto);
        }

        pedido.recalcularTotal();

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // Notificar observadores (Padrão Observer) via Spring ApplicationEventPublisher
        publicarEventoPedidoCriado(pedidoSalvo);

        return PedidoResponseDTO.deEntidade(pedidoSalvo);
    }

    /**
     * Retorna todos os pedidos registrados no sistema.
     */
    public List<PedidoResponseDTO> listarTodos() {
        return pedidoRepository.findAll().stream()
                .map(PedidoResponseDTO::deEntidade)
                .collect(Collectors.toList());
    }

    /**
     * Busca um pedido por ID e retorna seu DTO detalhado.
     */
    public PedidoResponseDTO buscarPorId(Long id) {
        return PedidoResponseDTO.deEntidade(buscarEntidadePorId(id));
    }

    /**
     * Busca a entidade Pedido por ID ou lança RecursoNaoEncontradoException.
     */
    public Pedido buscarEntidadePorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id));
    }

    /**
     * Retorna os pedidos filtrados por status (ex: RECEBIDO, EM_PREPARO, PRONTO).
     */
    public List<PedidoResponseDTO> buscarPorStatus(StatusPedido status) {
        return pedidoRepository.findByStatus(status).stream()
                .map(PedidoResponseDTO::deEntidade)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza o status operacional do pedido garantindo a máquina de estados e o fluxo de pagamentos:
     * 1. A transição deve ser válida conforme o ciclo de vida RECEBIDO -> EM_PREPARO -> PRONTO -> ENTREGUE.
     * 2. O pedido só pode ter o status alterado para preparo na cozinha (EM_PREPARO, PRONTO, ENTREGUE)
     *    após a confirmação do pagamento ter sido efetuada.
     *
     * @param pedidoId identificador do pedido.
     * @param novoStatus novo status a ser aplicado.
     * @return DTO com os dados atualizados do pedido.
     * @throws TransicaoStatusInvalidaException se a transição entre estados for ilegal.
     * @throws NegocioException se tentar iniciar o preparo sem que o pedido esteja pago.
     */
    @Transactional
    public PedidoResponseDTO atualizarStatus(Long pedidoId, StatusPedido novoStatus) {
        if (novoStatus == null) {
            throw new NegocioException("O novo status do pedido é obrigatório.");
        }

        Pedido pedido = buscarEntidadePorId(pedidoId);

        // Validar máquina de estados estrita
        if (!pedido.getStatus().podeTransitarPara(novoStatus)) {
            throw new TransicaoStatusInvalidaException(pedido.getStatus(), novoStatus);
        }

        if (novoStatus == StatusPedido.EM_PREPARO || novoStatus == StatusPedido.PRONTO || novoStatus == StatusPedido.ENTREGUE) {
            if (!pedido.isPago()) {
                throw new NegocioException("O pedido não pode ter status alterado para preparo na cozinha antes da confirmação do pagamento.");
            }
        }

        StatusPedido statusAnterior = pedido.getStatus();
        pedido.setStatus(novoStatus);
        if (novoStatus == StatusPedido.PRONTO && pedido.getDataHoraPronto() == null) {
            pedido.setDataHoraPronto(LocalDateTime.now());
        }
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // Notificar observadores sobre a alteração de status
        publicarEventoStatusAlterado(pedidoSalvo, statusAnterior, novoStatus);

        return PedidoResponseDTO.deEntidade(pedidoSalvo);
    }

    /**
     * Avança o pedido para o próximo estágio natural do fluxo da cozinha:
     * RECEBIDO -> EM_PREPARO -> PRONTO -> ENTREGUE.
     *
     * @param pedidoId identificador do pedido.
     * @return DTO com o pedido atualizado no próximo estágio.
     */
    @Transactional
    public PedidoResponseDTO avancarStatus(Long pedidoId) {
        Pedido pedido = buscarEntidadePorId(pedidoId);
        StatusPedido proximo = pedido.getStatus().proximoStatus();
        if (proximo == null) {
            throw new TransicaoStatusInvalidaException(
                    String.format("O pedido #%d com status '%s' não possui próxima etapa no fluxo de produção.",
                            pedidoId, pedido.getStatus())
            );
        }
        return atualizarStatus(pedidoId, proximo);
    }

    /**
     * Lista pedidos ativos para o painel operacional da cozinha (KDS).
     * Retorna pedidos nos estados RECEBIDO e EM_PREPARO ordenados pelo tempo de espera (mais antigos primeiro).
     *
     * @return lista de pedidos ativos com cálculo de tempo de espera, SLA e tags de customização.
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<PedidoKdsResponseDTO> listarPedidosAtivosKds() {
        return pedidoRepository.findByStatusInOrderByDataHoraAsc(List.of(StatusPedido.RECEBIDO, StatusPedido.EM_PREPARO))
                .stream()
                .map(PedidoKdsResponseDTO::deEntidade)
                .collect(Collectors.toList());
    }

    /**
     * Lista pedidos prontos aguardando entrega/retirada no balcão.
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<PedidoKdsResponseDTO> listarPedidosProntosKds() {
        return pedidoRepository.findByStatusInOrderByDataHoraAsc(List.of(StatusPedido.PRONTO))
                .stream()
                .map(PedidoKdsResponseDTO::deEntidade)
                .collect(Collectors.toList());
    }

    /**
     * Lista senhas agrupadas para o Painel de TV do Salão:
     * - emPreparo: pedidos com status RECEBIDO ou EM_PREPARO (mais antigos primeiro)
     * - prontos: pedidos com status PRONTO (mais recentes primeiro para destaque de chamada)
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.Map<String, List<PedidoResponseDTO>> listarPainelSenhas() {
        List<PedidoResponseDTO> emPreparo = pedidoRepository
                .findByStatusInOrderByDataHoraAsc(List.of(StatusPedido.RECEBIDO, StatusPedido.EM_PREPARO))
                .stream()
                .map(PedidoResponseDTO::deEntidade)
                .collect(Collectors.toList());

        List<PedidoResponseDTO> prontos = pedidoRepository
                .findByStatusInOrderByDataHoraAsc(List.of(StatusPedido.PRONTO))
                .stream()
                .sorted((a, b) -> {
                    java.time.LocalDateTime t1 = b.getDataHora() != null ? b.getDataHora() : java.time.LocalDateTime.MIN;
                    java.time.LocalDateTime t2 = a.getDataHora() != null ? a.getDataHora() : java.time.LocalDateTime.MIN;
                    return t1.compareTo(t2);
                })
                .map(PedidoResponseDTO::deEntidade)
                .collect(Collectors.toList());

        java.util.Map<String, List<PedidoResponseDTO>> resultado = new java.util.HashMap<>();
        resultado.put("emPreparo", emPreparo);
        resultado.put("prontos", prontos);
        return resultado;
    }

    /**
     * Atualiza o status do pedido a partir de uma ação no KDS, retornando o DTO específico da cozinha.
     * Caso o novoStatus seja nulo, avança automaticamente para a próxima etapa.
     *
     * @param pedidoId identificador do pedido.
     * @param novoStatus status de destino desejado (ou null para avançar automaticamente).
     * @return DTO específico do KDS com temporizador de SLA recalculado.
     */
    @Transactional
    public PedidoKdsResponseDTO atualizarStatusKds(Long pedidoId, StatusPedido novoStatus) {
        if (novoStatus == null) {
            return avancarStatusKds(pedidoId);
        }
        PedidoResponseDTO response = atualizarStatus(pedidoId, novoStatus);
        Pedido pedido = buscarEntidadePorId(response.getId());
        return PedidoKdsResponseDTO.deEntidade(pedido);
    }

    /**
     * Avança o pedido para a próxima etapa no display da cozinha (KDS).
     *
     * @param pedidoId identificador do pedido.
     * @return DTO específico do KDS com o próximo estágio aplicado.
     */
    @Transactional
    public PedidoKdsResponseDTO avancarStatusKds(Long pedidoId) {
        PedidoResponseDTO response = avancarStatus(pedidoId);
        Pedido pedido = buscarEntidadePorId(response.getId());
        return PedidoKdsResponseDTO.deEntidade(pedido);
    }

    private void publicarEventoPedidoCriado(Pedido pedido) {
        if (eventPublisher != null) {
            try {
                eventPublisher.publishEvent(new PedidoCriadoEvent(
                        pedido.getId(),
                        pedido.getNumeroSenha(),
                        pedido.getDataHora(),
                        pedido.getValorTotal(),
                        pedido.getNomeCliente(),
                        pedido.getCpfCliente()
                ));
            } catch (Exception e) {
                log.error("Falha ao publicar PedidoCriadoEvent para o pedido #{}: {}",
                        pedido.getId(), e.getMessage(), e);
            }
        }
    }

    private void publicarEventoStatusAlterado(Pedido pedido, StatusPedido statusAnterior, StatusPedido novoStatus) {
        if (eventPublisher != null) {
            try {
                eventPublisher.publishEvent(new PedidoStatusAlteradoEvent(
                        pedido.getId(),
                        pedido.getNumeroSenha(),
                        statusAnterior,
                        novoStatus,
                        LocalDateTime.now()
                ));
            } catch (Exception e) {
                log.error("Falha ao publicar PedidoStatusAlteradoEvent para o pedido #{}: {}",
                        pedido.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Valida os dados de customização enviados pelo cliente, recupera preços e regras estritas do banco de dados,
     * calcula o subtotal e retorna o DTO discriminado para exibição no Totem/Carrinho.
     *
     * Fórmula: subtotal = (precoBaseProduto + somaAdicionais + diferencaTamanho) * quantidade
     */
    public ItemPedidoResponseDTO calcularItem(CustomizacaoItemDTO dto) {
        ItemPedido item = montarItemPedido(dto);

        BigDecimal somaAdicionais = BigDecimal.ZERO;
        BigDecimal diferencaTamanho = BigDecimal.ZERO;
        List<String> remocoes = new ArrayList<>();

        for (ItemPedidoOpcao op : item.getOpcoes()) {
            if (op.getTipo() == TipoOpcao.TAMANHO_BATATA) {
                diferencaTamanho = diferencaTamanho.add(op.getPrecoExtra());
            } else if (op.getTipo() == TipoOpcao.ADICIONAL || op.getTipo() == TipoOpcao.BEBIDA) {
                somaAdicionais = somaAdicionais.add(op.getPrecoExtra());
            } else if (op.getTipo() == TipoOpcao.REMOCAO) {
                remocoes.add(op.getNomeOpcao());
            }
        }

        return ItemPedidoResponseDTO.deEntidade(item, somaAdicionais, diferencaTamanho, remocoes);
    }

    /**
     * Constrói e valida uma instância completa de ItemPedido com suas opções filhas (ItemPedidoOpcao).
     * Garante que preços vindos do front-end não sejam confiados e valida integridade contra o banco de dados.
     */
    public ItemPedido montarItemPedido(CustomizacaoItemDTO dto) {
        if (dto == null) {
            throw new NegocioException("Os dados de customização do item são obrigatórios.");
        }

        if (dto.getProdutoId() == null) {
            throw new NegocioException("O ID do produto é obrigatório.");
        }

        int quantidade = dto.getQuantidade() != null ? dto.getQuantidade() : 1;
        if (quantidade < 1) {
            throw new NegocioException("A quantidade mínima do item deve ser 1.");
        }

        Produto produto = produtoRepository.findById(dto.getProdutoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", dto.getProdutoId()));

        if (Boolean.FALSE.equals(produto.getAtivo())) {
            throw new NegocioException(String.format("O produto '%s' está inativo e não pode ser adicionado ao pedido.", produto.getNome()));
        }

        Integer estoqueAtual = produto.getQuantidadeEstoque();
        if (estoqueAtual == null || estoqueAtual < quantidade) {
            throw new NegocioException(String.format(
                    "Estoque insuficiente para o produto '%s'. Disponível: %d, Solicitado: %d.",
                    produto.getNome(), estoqueAtual != null ? estoqueAtual : 0, quantidade
            ));
        }

        Set<OpcaoAdicional> opcoesDisponiveis = produto.getOpcoesDisponiveis();
        boolean validarVinculoProduto = opcoesDisponiveis != null && !opcoesDisponiveis.isEmpty();

        BigDecimal precoBaseProduto = produto.getPreco();
        BigDecimal somaAdicionais = BigDecimal.ZERO;
        BigDecimal diferencaTamanho = BigDecimal.ZERO;

        List<ItemPedidoOpcao> opcoesCriadas = new ArrayList<>();
        List<String> remocoesFormatadas = new ArrayList<>();

        // 1. Processar Adicionais Pagos (+Bacon, +Cheddar, etc.)
        if (dto.getAdicionaisIds() != null) {
            for (Long adicionalId : dto.getAdicionaisIds()) {
                OpcaoAdicional opcao = buscarEValidarOpcao(adicionalId, produto, validarVinculoProduto, opcoesDisponiveis);
                BigDecimal precoExtra = opcao.getPrecoExtra() != null ? opcao.getPrecoExtra() : BigDecimal.ZERO;
                somaAdicionais = somaAdicionais.add(precoExtra);

                ItemPedidoOpcao itemOpcao = new ItemPedidoOpcao(opcao, opcao.getNome(), precoExtra, TipoOpcao.ADICIONAL);
                opcoesCriadas.add(itemOpcao);
            }
        }

        // 2. Processar Tamanho de Acompanhamento / Batata em Combos
        if (dto.getTamanhoBatataId() != null) {
            OpcaoAdicional opcaoTamanho = buscarEValidarOpcao(dto.getTamanhoBatataId(), produto, validarVinculoProduto, opcoesDisponiveis);
            BigDecimal precoDiff = opcaoTamanho.getPrecoExtra() != null ? opcaoTamanho.getPrecoExtra() : BigDecimal.ZERO;
            diferencaTamanho = diferencaTamanho.add(precoDiff);

            ItemPedidoOpcao itemOpcao = new ItemPedidoOpcao(opcaoTamanho, opcaoTamanho.getNome(), precoDiff, TipoOpcao.TAMANHO_BATATA);
            opcoesCriadas.add(itemOpcao);
        }

        // 3. Processar Bebida em Combos
        if (dto.getBebidaId() != null) {
            OpcaoAdicional opcaoBebida = buscarEValidarOpcao(dto.getBebidaId(), produto, validarVinculoProduto, opcoesDisponiveis);
            BigDecimal precoBebida = opcaoBebida.getPrecoExtra() != null ? opcaoBebida.getPrecoExtra() : BigDecimal.ZERO;
            somaAdicionais = somaAdicionais.add(precoBebida);

            ItemPedidoOpcao itemOpcao = new ItemPedidoOpcao(opcaoBebida, opcaoBebida.getNome(), precoBebida, TipoOpcao.BEBIDA);
            opcoesCriadas.add(itemOpcao);
        }

        // 4. Processar Remoções de Ingredientes via IDs (ex: cebola, picles)
        if (dto.getRemocoesIds() != null) {
            for (Long remocaoId : dto.getRemocoesIds()) {
                OpcaoAdicional opcaoRemocao = buscarEValidarOpcao(remocaoId, produto, validarVinculoProduto, opcoesDisponiveis);

                if (!Boolean.TRUE.equals(opcaoRemocao.getPermiteRemocao()) && opcaoRemocao.getTipo() != TipoOpcao.REMOCAO) {
                    throw new NegocioException(String.format("A opção '%s' não é um ingrediente que permite remoção.", opcaoRemocao.getNome()));
                }

                String nomeFormatado = formatarTextoRemocao(opcaoRemocao.getNome());
                if (!remocoesFormatadas.contains(nomeFormatado)) {
                    remocoesFormatadas.add(nomeFormatado);
                }

                // Remoções de ingredientes NÃO alteram o valor (preço extra = 0.00)
                ItemPedidoOpcao itemOpcao = new ItemPedidoOpcao(opcaoRemocao, nomeFormatado, BigDecimal.ZERO, TipoOpcao.REMOCAO);
                opcoesCriadas.add(itemOpcao);
            }
        }

        // 5. Processar Remoções textuais diretas do front-end (ex: "Sem cebola", "Sem picles")
        if (dto.getRemocoes() != null) {
            for (String textoRemocao : dto.getRemocoes()) {
                if (textoRemocao != null && !textoRemocao.trim().isEmpty()) {
                    String nomeFormatado = formatarTextoRemocao(textoRemocao.trim());
                    if (!remocoesFormatadas.contains(nomeFormatado)) {
                        remocoesFormatadas.add(nomeFormatado);
                        ItemPedidoOpcao itemOpcao = new ItemPedidoOpcao(null, nomeFormatado, BigDecimal.ZERO, TipoOpcao.REMOCAO);
                        opcoesCriadas.add(itemOpcao);
                    }
                }
            }
        }

        // 6. Cálculo Estrito de Valores
        // precoUnitario = precoBaseProduto + somaAdicionais + diferencaTamanho
        BigDecimal precoUnitario = precoBaseProduto.add(somaAdicionais).add(diferencaTamanho);
        // subtotal = precoUnitario * quantidade
        BigDecimal subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));

        // 7. Montagem das Observações combinando remoções e notas livres
        String observacoesFinais = comporObservacoes(remocoesFormatadas, dto.getObservacoes());

        // 8. Construção da Entidade ItemPedido
        ItemPedido item = new ItemPedido();
        item.setProduto(produto);
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(precoUnitario);
        item.setSubtotal(subtotal);
        item.setObservacoes(observacoesFinais);

        for (ItemPedidoOpcao opcao : opcoesCriadas) {
            item.adicionarOpcao(opcao);
        }

        return item;
    }

    private OpcaoAdicional buscarEValidarOpcao(Long opcaoId, Produto produto, boolean validarVinculo, Set<OpcaoAdicional> opcoesDisponiveis) {
        OpcaoAdicional opcao = opcaoAdicionalRepository.findById(opcaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("OpcaoAdicional", opcaoId));

        if (Boolean.FALSE.equals(opcao.getAtivo())) {
            throw new NegocioException(String.format("A opção '%s' está inativa.", opcao.getNome()));
        }

        if (validarVinculo && !opcoesDisponiveis.contains(opcao)) {
            throw new NegocioException(String.format("A opção '%s' não está disponível para o produto '%s'.", opcao.getNome(), produto.getNome()));
        }

        return opcao;
    }

    private String formatarTextoRemocao(String nome) {
        if (nome == null) return "";
        if (nome.toLowerCase().startsWith("sem ")) {
            return nome;
        }
        return "Sem " + nome;
    }

    private String comporObservacoes(List<String> remocoes, String observacoesLivres) {
        StringBuilder sb = new StringBuilder();

        if (remocoes != null && !remocoes.isEmpty()) {
            sb.append(String.join(", ", remocoes));
        }

        if (observacoesLivres != null && !observacoesLivres.trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(". Obs: ");
            }
            sb.append(observacoesLivres.trim());
        }

        return sb.length() > 0 ? sb.toString() : null;
    }
}

