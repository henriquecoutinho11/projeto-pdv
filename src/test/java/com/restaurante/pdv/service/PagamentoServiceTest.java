package com.restaurante.pdv.service;

import com.restaurante.pdv.dto.request.ConfirmacaoPagamentoDTO;
import com.restaurante.pdv.dto.request.SimulacaoCartaoDTO;
import com.restaurante.pdv.dto.response.CobrancaPixResponseDTO;
import com.restaurante.pdv.dto.response.PagamentoResponseDTO;
import com.restaurante.pdv.exception.NegocioException;
import com.restaurante.pdv.exception.RecursoNaoEncontradoException;
import com.restaurante.pdv.model.entity.*;
import com.restaurante.pdv.model.repository.CobrancaPixRepository;
import com.restaurante.pdv.model.repository.PedidoRepository;
import com.restaurante.pdv.model.service.PagamentoService;
import com.restaurante.pdv.model.service.PixEmvService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - PagamentoService")
class PagamentoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private CobrancaPixRepository cobrancaPixRepository;

    private PixEmvService pixEmvService;
    private PagamentoService pagamentoService;

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        pixEmvService = new PixEmvService();
        pagamentoService = new PagamentoService(pedidoRepository, cobrancaPixRepository, pixEmvService);

        ReflectionTestUtils.setField(pagamentoService, "chavePix", "financeiro@antuneslanches.com.br");
        ReflectionTestUtils.setField(pagamentoService, "nomeRecebedor", "Antunes Lanches");
        ReflectionTestUtils.setField(pagamentoService, "cidadeRecebedor", "Sao Paulo");
        ReflectionTestUtils.setField(pagamentoService, "tempoExpiracaoMinutos", 5);

        pedido = new Pedido();
        pedido.setId(10L);
        pedido.setNumeroSenha(42);
        pedido.setStatus(StatusPedido.RECEBIDO);
        pedido.setValorTotal(new BigDecimal("45.00"));
        pedido.setPago(Boolean.FALSE);
    }

    @Test
    @DisplayName("Deve gerar cobrança PIX com janela de expiração de 5 minutos")
    void deveGerarCobrancaPixComSucesso() {
        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedido));
        when(cobrancaPixRepository.findTopByPedidoIdOrderByDataCriacaoDesc(10L)).thenReturn(Optional.empty());
        when(cobrancaPixRepository.save(any(CobrancaPix.class))).thenAnswer(invocation -> {
            CobrancaPix c = invocation.getArgument(0);
            c.setId(100L);
            return c;
        });

        CobrancaPixResponseDTO response = pagamentoService.gerarCobrancaPix(10L);

        assertNotNull(response);
        assertEquals(10L, response.getPedidoId());
        assertEquals(new BigDecimal("45.00"), response.getValor());
        assertNotNull(response.getPayloadEmv());
        assertNotNull(response.getQrCodeBase64());
        assertFalse(response.getExpirado());
        assertFalse(response.getPago());
        assertTrue(response.getSegundosRestantes() > 280 && response.getSegundosRestantes() <= 300);

        verify(cobrancaPixRepository, times(1)).save(any(CobrancaPix.class));
    }

    @Test
    @DisplayName("Deve reutilizar cobrança PIX existente quando ainda estiver ativa e dentro dos 5 minutos")
    void deveReutilizarCobrancaPixAtiva() {
        CobrancaPix cobrancaAtiva = new CobrancaPix(
                pedido, "TX123", "financeiro@antuneslanches.com.br", new BigDecimal("45.00"),
                "000201...6304ABCD", "base64qrcode", LocalDateTime.now(), LocalDateTime.now().plusMinutes(4)
        );
        cobrancaAtiva.setId(99L);

        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedido));
        when(cobrancaPixRepository.findTopByPedidoIdOrderByDataCriacaoDesc(10L)).thenReturn(Optional.of(cobrancaAtiva));

        CobrancaPixResponseDTO response = pagamentoService.gerarCobrancaPix(10L);

        assertEquals(99L, response.getId());
        assertEquals("TX123", response.getTxId());
        verify(cobrancaPixRepository, never()).save(any(CobrancaPix.class));
    }

    @Test
    @DisplayName("Deve rejeitar geração de PIX para pedido já pago")
    void deveRejeitarGeracaoPixParaPedidoJaPago() {
        pedido.setPago(Boolean.TRUE);
        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedido));

        NegocioException ex = assertThrows(NegocioException.class, () -> pagamentoService.gerarCobrancaPix(10L));
        assertTrue(ex.getMessage().contains("já se encontra pago"));
    }

    @Test
    @DisplayName("Deve rejeitar geração de PIX para pedido cancelado")
    void deveRejeitarGeracaoPixParaPedidoCancelado() {
        pedido.setStatus(StatusPedido.CANCELADO);
        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedido));

        NegocioException ex = assertThrows(NegocioException.class, () -> pagamentoService.gerarCobrancaPix(10L));
        assertTrue(ex.getMessage().contains("Não é possível gerar cobrança PIX para o pedido cancelado"));
    }

    @Test
    @DisplayName("Deve confirmar pagamento via PIX com sucesso")
    void deveConfirmarPagamentoPixComSucesso() {
        CobrancaPix cobrancaAtiva = new CobrancaPix(
                pedido, "TX123", "chave@pix.com", new BigDecimal("45.00"),
                "payload", "base64", LocalDateTime.now(), LocalDateTime.now().plusMinutes(5)
        );

        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedido));
        when(cobrancaPixRepository.findTopByPedidoIdOrderByDataCriacaoDesc(10L)).thenReturn(Optional.of(cobrancaAtiva));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConfirmacaoPagamentoDTO dto = new ConfirmacaoPagamentoDTO(FormaPagamento.PIX, new BigDecimal("45.00"), "TX-PIX-123");

        PagamentoResponseDTO response = pagamentoService.confirmarPagamento(10L, dto);

        assertNotNull(response);
        assertTrue(response.getPago());
        assertEquals(FormaPagamento.PIX, response.getFormaPagamento());
        assertEquals(new BigDecimal("45.00"), response.getValorPago());
        assertNotNull(response.getDataHoraPagamento());

        assertTrue(cobrancaAtiva.getPago());
        assertTrue(pedido.isPago());
        verify(pedidoRepository).save(pedido);
        verify(cobrancaPixRepository).save(cobrancaAtiva);
    }

    @Test
    @DisplayName("Deve rejeitar confirmação PIX quando a cobrança estiver expirada")
    void deveRejeitarConfirmacaoPixExpirada() {
        CobrancaPix cobrancaExpirada = new CobrancaPix(
                pedido, "TX123", "chave@pix.com", new BigDecimal("45.00"),
                "payload", "base64", LocalDateTime.now().minusMinutes(6), LocalDateTime.now().minusMinutes(1)
        );

        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedido));
        when(cobrancaPixRepository.findTopByPedidoIdOrderByDataCriacaoDesc(10L)).thenReturn(Optional.of(cobrancaExpirada));

        ConfirmacaoPagamentoDTO dto = new ConfirmacaoPagamentoDTO(FormaPagamento.PIX, new BigDecimal("45.00"));

        NegocioException ex = assertThrows(NegocioException.class, () -> pagamentoService.confirmarPagamento(10L, dto));
        assertTrue(ex.getMessage().contains("Cobrança PIX expirada"));
    }

    @Test
    @DisplayName("Deve rejeitar confirmação de pagamento com valor divergente do total do pedido")
    void deveRejeitarValorDivergente() {
        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedido));

        ConfirmacaoPagamentoDTO dto = new ConfirmacaoPagamentoDTO(FormaPagamento.CARTAO_CREDITO, new BigDecimal("30.00"));

        NegocioException ex = assertThrows(NegocioException.class, () -> pagamentoService.confirmarPagamento(10L, dto));
        assertTrue(ex.getMessage().contains("difere do valor total do pedido"));
    }

    @Test
    @DisplayName("Deve confirmar pagamento via Cartão de Crédito")
    void deveConfirmarPagamentoCartaoCredito() {
        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConfirmacaoPagamentoDTO dto = new ConfirmacaoPagamentoDTO(FormaPagamento.CARTAO_CREDITO, new BigDecimal("45.00"), "AUT-12345");

        PagamentoResponseDTO response = pagamentoService.confirmarPagamento(10L, dto);

        assertNotNull(response);
        assertTrue(response.getPago());
        assertEquals(FormaPagamento.CARTAO_CREDITO, response.getFormaPagamento());
        assertEquals("AUT-12345", response.getCodigoTransacao());
    }

    @Test
    @DisplayName("Deve simular pagamento com cartão touch com geração automática de NSU")
    void deveSimularPagamentoCartaoTouch() {
        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SimulacaoCartaoDTO simulacao = new SimulacaoCartaoDTO(FormaPagamento.CARTAO_DEBITO, "VISA");
        simulacao.setTipoOperacao("APROXIMACAO");

        PagamentoResponseDTO response = pagamentoService.simularPagamentoCartao(10L, simulacao);

        assertNotNull(response);
        assertTrue(response.getPago());
        assertEquals(FormaPagamento.CARTAO_DEBITO, response.getFormaPagamento());
        assertTrue(response.getCodigoTransacao().startsWith("NSU-"));
    }

    @Test
    @DisplayName("Deve consultar status do pagamento retornando pendente ou pago")
    void deveConsultarStatusPagamento() {
        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedido));

        PagamentoResponseDTO statusPendente = pagamentoService.consultarStatusPagamento(10L);
        assertFalse(statusPendente.getPago());
        assertEquals("Aguardando pagamento.", statusPendente.getMensagem());

        pedido.setPago(Boolean.TRUE);
        PagamentoResponseDTO statusPago = pagamentoService.consultarStatusPagamento(10L);
        assertTrue(statusPago.getPago());
        assertEquals("Pagamento efetuado com sucesso.", statusPago.getMensagem());
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException para pedido inexistente")
    void deveLancarExcecaoParaPedidoInexistente() {
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> pagamentoService.gerarCobrancaPix(999L));
        assertThrows(RecursoNaoEncontradoException.class, () ->
                pagamentoService.confirmarPagamento(999L, new ConfirmacaoPagamentoDTO(FormaPagamento.PIX, BigDecimal.TEN))
        );
    }
}
