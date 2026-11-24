package com.restaurante.pdv.service;

import com.restaurante.pdv.dto.response.*;
import com.restaurante.pdv.exception.NegocioException;
import com.restaurante.pdv.model.entity.FormaPagamento;
import com.restaurante.pdv.model.entity.Pedido;
import com.restaurante.pdv.model.entity.StatusPedido;
import com.restaurante.pdv.model.repository.ItemPedidoRepository;
import com.restaurante.pdv.model.repository.PedidoRepository;
import com.restaurante.pdv.model.service.RelatorioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ItemPedidoRepository itemPedidoRepository;

    @InjectMocks
    private RelatorioService relatorioService;

    @Test
    @DisplayName("Deve gerar relatório de faturamento consolidado com sucesso")
    void deveGerarRelatorioFaturamentoComSucesso() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 24, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 11, 24, 23, 59);

        List<Object[]> faturamento = new ArrayList<>();
        faturamento.add(new Object[]{10L, new BigDecimal("350.00")});

        List<Object[]> porForma = new ArrayList<>();
        porForma.add(new Object[]{FormaPagamento.PIX, 6L, new BigDecimal("200.00")});
        porForma.add(new Object[]{FormaPagamento.CARTAO_CREDITO, 4L, new BigDecimal("150.00")});

        List<Object[]> porStatus = new ArrayList<>();
        porStatus.add(new Object[]{StatusPedido.ENTREGUE, 8L, new BigDecimal("280.00")});
        porStatus.add(new Object[]{StatusPedido.PRONTO, 2L, new BigDecimal("70.00")});

        when(pedidoRepository.somarFaturamentoEPedidosPorPeriodo(inicio, fim)).thenReturn(faturamento);
        when(pedidoRepository.agruparPorFormaPagamentoNoPeriodo(inicio, fim)).thenReturn(porForma);
        when(pedidoRepository.agruparPorStatusNoPeriodo(inicio, fim)).thenReturn(porStatus);

        RelatorioFaturamentoDTO resultado = relatorioService.gerarRelatorioFaturamento(inicio, fim);

        assertNotNull(resultado);
        assertEquals(10L, resultado.totalPedidos());
        assertEquals(new BigDecimal("350.00"), resultado.totalFaturado());
        assertEquals(new BigDecimal("35.00"), resultado.ticketMedio());
        assertEquals(2, resultado.vendasPorFormaPagamento().size());
        assertEquals(2, resultado.vendasPorStatus().size());
    }

    @Test
    @DisplayName("Deve lançar NegocioException quando data inicial for posterior à data final")
    void deveLancarExcecaoQuandoDataInicioPosteriorADataFim() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 25, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 11, 24, 23, 59);

        assertThrows(NegocioException.class, () -> relatorioService.gerarRelatorioFaturamento(inicio, fim));
    }

    @Test
    @DisplayName("Deve gerar fechamento de caixa diário onde a soma das formas bate exatamente com o total faturado")
    void deveGerarFechamentoCaixaCorretamente() {
        LocalDate hoje = LocalDate.of(2026, 11, 24);

        List<Object[]> porForma = new ArrayList<>();
        porForma.add(new Object[]{FormaPagamento.DINHEIRO, 2L, new BigDecimal("50.00")});
        porForma.add(new Object[]{FormaPagamento.PIX, 5L, new BigDecimal("150.00")});
        porForma.add(new Object[]{FormaPagamento.CARTAO_CREDITO, 3L, new BigDecimal("120.00")});
        porForma.add(new Object[]{FormaPagamento.CARTAO_DEBITO, 1L, new BigDecimal("30.00")});

        when(pedidoRepository.agruparPorFormaPagamentoNoPeriodo(any(), any())).thenReturn(porForma);

        FechamentoCaixaDTO fechamento = relatorioService.gerarFechamentoCaixa(hoje);

        assertNotNull(fechamento);
        assertEquals(hoje, fechamento.data());
        assertEquals(11L, fechamento.totalPedidos());
        assertEquals(new BigDecimal("350.00"), fechamento.totalFaturado());
        assertEquals(new BigDecimal("50.00"), fechamento.totalDinheiro());
        assertEquals(new BigDecimal("150.00"), fechamento.totalPix());
        assertEquals(new BigDecimal("120.00"), fechamento.totalCartaoCredito());
        assertEquals(new BigDecimal("30.00"), fechamento.totalCartaoDebito());

        // Critério de aceite: soma exata das formas de pagamento
        BigDecimal somaFormas = fechamento.totalDinheiro()
                .add(fechamento.totalPix())
                .add(fechamento.totalCartaoCredito())
                .add(fechamento.totalCartaoDebito());
        assertEquals(fechamento.totalFaturado(), somaFormas);
    }

    @Test
    @DisplayName("Deve classificar itens na Curva ABC com base no faturamento acumulado")
    void deveCalcularRankingProdutosComCurvaAbc() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 24, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 11, 24, 23, 59);

        List<Object[]> vendas = new ArrayList<>();
        // Item 1: 60% do total
        vendas.add(new Object[]{1L, "Combo Especial", "Combos", 20L, new BigDecimal("600.00")});
        // Item 2: 25% do total (acumulado 85% -> B)
        vendas.add(new Object[]{2L, "X-Burguer", "Hambúrgueres", 15L, new BigDecimal("250.00")});
        // Item 3: 15% do total (acumulado 100% -> C)
        vendas.add(new Object[]{3L, "Refrigerante", "Bebidas", 30L, new BigDecimal("150.00")});

        when(itemPedidoRepository.findRankingProdutosMaisVendidosSemLimite(inicio, fim)).thenReturn(vendas);

        List<ItemMaisVendidoDTO> ranking = relatorioService.obterRankingProdutosMaisVendidos(inicio, fim, 10);

        assertNotNull(ranking);
        assertEquals(3, ranking.size());

        assertEquals("Combo Especial", ranking.get(0).nomeProduto());
        assertEquals("A", ranking.get(0).classificacaoAbc());

        assertEquals("X-Burguer", ranking.get(1).nomeProduto());
        assertEquals("B", ranking.get(1).classificacaoAbc());

        assertEquals("Refrigerante", ranking.get(2).nomeProduto());
        assertEquals("C", ranking.get(2).classificacaoAbc());
    }

    @Test
    @DisplayName("Deve calcular o tempo médio de preparo dos pedidos na cozinha")
    void deveCalcularTempoMedioPreparoCorretamente() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 24, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 11, 24, 23, 59);

        Pedido p1 = new Pedido();
        p1.setDataHora(LocalDateTime.of(2026, 11, 24, 12, 0));
        p1.setDataHoraPronto(LocalDateTime.of(2026, 11, 24, 12, 10)); // 10 min

        Pedido p2 = new Pedido();
        p2.setDataHora(LocalDateTime.of(2026, 11, 24, 12, 5));
        p2.setDataHoraPronto(LocalDateTime.of(2026, 11, 24, 12, 25)); // 20 min

        when(pedidoRepository.findPedidosComTempoPreparoNoPeriodo(inicio, fim)).thenReturn(List.of(p1, p2));

        TempoMedioPreparoDTO tempo = relatorioService.calcularTempoMedioPreparo(inicio, fim);

        assertNotNull(tempo);
        assertEquals(2L, tempo.totalPedidosAvaliados());
        assertEquals(15.0, tempo.tempoMedioMinutos());
        assertEquals(10L, tempo.tempoMinimoMinutos());
        assertEquals(20L, tempo.tempoMaximoMinutos());
    }

    @Test
    @DisplayName("Deve gerar resumo geral de vendas consolidado com sucesso")
    void deveGerarResumoGeralComSucesso() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 24, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 11, 24, 23, 59);

        List<Object[]> faturamento = new ArrayList<>();
        faturamento.add(new Object[]{5L, new BigDecimal("250.00")});
        when(pedidoRepository.somarFaturamentoEPedidosPorPeriodo(inicio, fim)).thenReturn(faturamento);
        when(pedidoRepository.agruparPorFormaPagamentoNoPeriodo(inicio, fim)).thenReturn(Collections.emptyList());
        when(pedidoRepository.agruparPorStatusNoPeriodo(inicio, fim)).thenReturn(Collections.emptyList());
        when(pedidoRepository.findPedidosComTempoPreparoNoPeriodo(inicio, fim)).thenReturn(Collections.emptyList());
        when(itemPedidoRepository.findRankingProdutosMaisVendidosSemLimite(inicio, fim)).thenReturn(Collections.emptyList());

        ResumoVendasDTO resumo = relatorioService.gerarResumoGeral(inicio, fim);

        assertNotNull(resumo);
        assertEquals(5L, resumo.totalPedidos());
        assertEquals(new BigDecimal("250.00"), resumo.totalFaturado());
        assertEquals(new BigDecimal("50.00"), resumo.ticketMedio());
    }
}
