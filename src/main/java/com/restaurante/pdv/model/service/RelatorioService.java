package com.restaurante.pdv.model.service;

import com.restaurante.pdv.dto.response.*;
import com.restaurante.pdv.exception.NegocioException;
import com.restaurante.pdv.model.entity.FormaPagamento;
import com.restaurante.pdv.model.entity.Pedido;
import com.restaurante.pdv.model.entity.StatusPedido;
import com.restaurante.pdv.model.repository.ItemPedidoRepository;
import com.restaurante.pdv.model.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço responsável pela geração de relatórios analíticos, fechamento de caixa e métricas operacionais.
 */
@Service
@Transactional(readOnly = true)
public class RelatorioService {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public RelatorioService(PedidoRepository pedidoRepository, ItemPedidoRepository itemPedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    /**
     * Gera relatório de faturamento discriminado por forma de pagamento e status operacional no período.
     */
    public RelatorioFaturamentoDTO gerarRelatorioFaturamento(LocalDateTime dataInicio, LocalDateTime dataFim) {
        Periodo periodo = normalizarPeriodo(dataInicio, dataFim);

        List<Object[]> faturamentoAgregado = pedidoRepository.somarFaturamentoEPedidosPorPeriodo(
                periodo.inicio(), periodo.fim()
        );

        long totalPedidos = 0L;
        BigDecimal totalFaturado = BigDecimal.ZERO;

        if (faturamentoAgregado != null && !faturamentoAgregado.isEmpty()) {
            Object[] row = faturamentoAgregado.get(0);
            if (row != null && row.length >= 2) {
                totalPedidos = row[0] instanceof Number n ? n.longValue() : 0L;
                totalFaturado = extrairBigDecimal(row[1]);
            }
        }

        BigDecimal ticketMedio = calcularTicketMedio(totalFaturado, totalPedidos);

        List<VendasPorFormaPagamentoDTO> porForma = obterVendasPorFormaPagamento(periodo.inicio(), periodo.fim(), totalFaturado);
        List<VendasPorStatusDTO> porStatus = obterVendasPorStatus(periodo.inicio(), periodo.fim());

        return new RelatorioFaturamentoDTO(
                periodo.inicio(),
                periodo.fim(),
                totalPedidos,
                totalFaturado,
                ticketMedio,
                porForma,
                porStatus
        );
    }

    /**
     * Gera o fechamento diário do caixa para a data especificada.
     * Garante que a soma das formas de pagamento seja exatamente igual ao faturamento total diário.
     */
    public FechamentoCaixaDTO gerarFechamentoCaixa(LocalDate data) {
        LocalDate dataReferencia = data != null ? data : LocalDate.now();
        LocalDateTime inicio = dataReferencia.atStartOfDay();
        LocalDateTime fim = dataReferencia.atTime(LocalTime.MAX);

        List<Object[]> resultadosFormas = pedidoRepository.agruparPorFormaPagamentoNoPeriodo(inicio, fim);

        BigDecimal totalDinheiro = BigDecimal.ZERO;
        BigDecimal totalPix = BigDecimal.ZERO;
        BigDecimal totalCartaoCredito = BigDecimal.ZERO;
        BigDecimal totalCartaoDebito = BigDecimal.ZERO;
        long totalPedidos = 0L;

        Map<FormaPagamento, Long> contagemPorForma = new EnumMap<>(FormaPagamento.class);
        Map<FormaPagamento, BigDecimal> valorPorForma = new EnumMap<>(FormaPagamento.class);

        if (resultadosFormas != null) {
            for (Object[] row : resultadosFormas) {
                if (row == null || row.length < 3) continue;
                FormaPagamento fp = (FormaPagamento) row[0];
                long qtd = row[1] instanceof Number n ? n.longValue() : 0L;
                BigDecimal valor = extrairBigDecimal(row[2]);

                totalPedidos += qtd;
                if (fp != null) {
                    contagemPorForma.put(fp, qtd);
                    valorPorForma.put(fp, valor);

                    switch (fp) {
                        case DINHEIRO -> totalDinheiro = totalDinheiro.add(valor);
                        case PIX -> totalPix = totalPix.add(valor);
                        case CARTAO_CREDITO -> totalCartaoCredito = totalCartaoCredito.add(valor);
                        case CARTAO_DEBITO -> totalCartaoDebito = totalCartaoDebito.add(valor);
                    }
                }
            }
        }

        BigDecimal totalFaturado = totalDinheiro.add(totalPix).add(totalCartaoCredito).add(totalCartaoDebito);
        BigDecimal ticketMedio = calcularTicketMedio(totalFaturado, totalPedidos);

        List<VendasPorFormaPagamentoDTO> detalhes = new ArrayList<>();
        for (FormaPagamento fp : FormaPagamento.values()) {
            long qtd = contagemPorForma.getOrDefault(fp, 0L);
            BigDecimal valor = valorPorForma.getOrDefault(fp, BigDecimal.ZERO);
            double pct = totalFaturado.compareTo(BigDecimal.ZERO) > 0
                    ? valor.multiply(BigDecimal.valueOf(100)).divide(totalFaturado, 2, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;
            detalhes.add(new VendasPorFormaPagamentoDTO(fp, fp.getDescricao(), qtd, valor, pct));
        }

        return new FechamentoCaixaDTO(
                dataReferencia,
                totalPedidos,
                totalFaturado,
                ticketMedio,
                totalDinheiro,
                totalPix,
                totalCartaoCredito,
                totalCartaoDebito,
                detalhes
        );
    }

    /**
     * Retorna o ranking de produtos mais vendidos no período com cálculo da Curva ABC (Pareto).
     * Classe A: até ~70% do faturamento acumulado.
     * Classe B: entre 70% e 90% do faturamento acumulado.
     * Classe C: restante (90% a 100%).
     */
    public List<ItemMaisVendidoDTO> obterRankingProdutosMaisVendidos(LocalDateTime dataInicio, LocalDateTime dataFim, Integer limite) {
        Periodo periodo = normalizarPeriodo(dataInicio, dataFim);

        List<Object[]> resultados = itemPedidoRepository.findRankingProdutosMaisVendidosSemLimite(
                periodo.inicio(), periodo.fim()
        );

        if (resultados == null || resultados.isEmpty()) {
            return Collections.emptyList();
        }

        BigDecimal faturamentoTotalItens = BigDecimal.ZERO;
        List<ItemBruto> itensBrutos = new ArrayList<>();

        for (Object[] row : resultados) {
            Long produtoId = row[0] instanceof Number n ? n.longValue() : null;
            String nomeProduto = row[1] != null ? row[1].toString() : "Produto #" + produtoId;
            String categoriaNome = row[2] != null ? row[2].toString() : "Sem Categoria";
            Long quantidadeVendida = row[3] instanceof Number n ? n.longValue() : 0L;
            BigDecimal subtotal = extrairBigDecimal(row[4]);

            faturamentoTotalItens = faturamentoTotalItens.add(subtotal);
            itensBrutos.add(new ItemBruto(produtoId, nomeProduto, categoriaNome, quantidadeVendida, subtotal));
        }

        // Ordenar decrescente por valor faturado para Curva ABC consistente
        itensBrutos.sort((a, b) -> b.valorTotal().compareTo(a.valorTotal()));

        List<ItemMaisVendidoDTO> ranking = new ArrayList<>();
        double acumulado = 0.0;

        for (int i = 0; i < itensBrutos.size(); i++) {
            ItemBruto item = itensBrutos.get(i);
            double percentual = faturamentoTotalItens.compareTo(BigDecimal.ZERO) > 0
                    ? item.valorTotal().multiply(BigDecimal.valueOf(100)).divide(faturamentoTotalItens, 2, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;

            acumulado += percentual;
            if (i == itensBrutos.size() - 1 && acumulado > 0) {
                acumulado = 100.0;
            }

            String classeAbc;
            if (acumulado <= 70.0 || i == 0) {
                classeAbc = "A";
            } else if (acumulado <= 90.0) {
                classeAbc = "B";
            } else {
                classeAbc = "C";
            }

            ranking.add(new ItemMaisVendidoDTO(
                    item.produtoId(),
                    item.nomeProduto(),
                    item.categoriaNome(),
                    item.quantidadeVendida(),
                    item.valorTotal(),
                    percentual,
                    Math.min(100.0, Math.round(acumulado * 100.0) / 100.0),
                    classeAbc
            ));
        }

        if (limite != null && limite > 0 && limite < ranking.size()) {
            return ranking.subList(0, limite);
        }

        return ranking;
    }

    /**
     * Calcula métricas de tempo de preparo dos pedidos concluídos na cozinha (KDS).
     * Avalia a diferença de tempo entre dataHora (recebido) e dataHoraPronto.
     */
    public TempoMedioPreparoDTO calcularTempoMedioPreparo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        Periodo periodo = normalizarPeriodo(dataInicio, dataFim);

        List<Pedido> pedidosProntos = pedidoRepository.findPedidosComTempoPreparoNoPeriodo(
                periodo.inicio(), periodo.fim()
        );

        if (pedidosProntos == null || pedidosProntos.isEmpty()) {
            return new TempoMedioPreparoDTO(periodo.inicio(), periodo.fim(), 0L, 0.0, 0L, 0L);
        }

        List<Long> temposMinutos = pedidosProntos.stream()
                .filter(p -> p.getDataHora() != null && p.getDataHoraPronto() != null)
                .map(p -> Math.max(0L, Duration.between(p.getDataHora(), p.getDataHoraPronto()).toMinutes()))
                .toList();

        if (temposMinutos.isEmpty()) {
            return new TempoMedioPreparoDTO(periodo.inicio(), periodo.fim(), 0L, 0.0, 0L, 0L);
        }

        long soma = temposMinutos.stream().mapToLong(Long::longValue).sum();
        double media = (double) soma / temposMinutos.size();
        media = Math.round(media * 100.0) / 100.0;

        long minimo = temposMinutos.stream().mapToLong(Long::longValue).min().orElse(0L);
        long maximo = temposMinutos.stream().mapToLong(Long::longValue).max().orElse(0L);

        return new TempoMedioPreparoDTO(
                periodo.inicio(),
                periodo.fim(),
                (long) temposMinutos.size(),
                media,
                minimo,
                maximo
        );
    }

    /**
     * Gera visão executiva consolidada (Resumo Geral de Vendas).
     */
    public ResumoVendasDTO gerarResumoGeral(LocalDateTime dataInicio, LocalDateTime dataFim) {
        Periodo periodo = normalizarPeriodo(dataInicio, dataFim);

        RelatorioFaturamentoDTO faturamento = gerarRelatorioFaturamento(periodo.inicio(), periodo.fim());
        TempoMedioPreparoDTO tempoPreparo = calcularTempoMedioPreparo(periodo.inicio(), periodo.fim());
        List<ItemMaisVendidoDTO> top5 = obterRankingProdutosMaisVendidos(periodo.inicio(), periodo.fim(), 5);

        return new ResumoVendasDTO(
                periodo.inicio(),
                periodo.fim(),
                faturamento.totalFaturado(),
                faturamento.totalPedidos(),
                faturamento.ticketMedio(),
                tempoPreparo.tempoMedioMinutos(),
                top5,
                faturamento.vendasPorFormaPagamento()
        );
    }

    private List<VendasPorFormaPagamentoDTO> obterVendasPorFormaPagamento(LocalDateTime inicio, LocalDateTime fim, BigDecimal totalFaturado) {
        List<Object[]> resultados = pedidoRepository.agruparPorFormaPagamentoNoPeriodo(inicio, fim);
        List<VendasPorFormaPagamentoDTO> dtos = new ArrayList<>();

        if (resultados != null) {
            for (Object[] row : resultados) {
                if (row == null || row.length < 3) continue;
                FormaPagamento fp = (FormaPagamento) row[0];
                long qtd = row[1] instanceof Number n ? n.longValue() : 0L;
                BigDecimal valor = extrairBigDecimal(row[2]);

                double pct = totalFaturado != null && totalFaturado.compareTo(BigDecimal.ZERO) > 0
                        ? valor.multiply(BigDecimal.valueOf(100)).divide(totalFaturado, 2, RoundingMode.HALF_UP).doubleValue()
                        : 0.0;

                dtos.add(new VendasPorFormaPagamentoDTO(
                        fp,
                        fp != null ? fp.getDescricao() : "Indefinido",
                        qtd,
                        valor,
                        pct
                ));
            }
        }
        return dtos;
    }

    private List<VendasPorStatusDTO> obterVendasPorStatus(LocalDateTime inicio, LocalDateTime fim) {
        List<Object[]> resultados = pedidoRepository.agruparPorStatusNoPeriodo(inicio, fim);
        List<VendasPorStatusDTO> dtos = new ArrayList<>();

        long totalGeralPedidos = 0L;
        if (resultados != null) {
            for (Object[] row : resultados) {
                if (row != null && row.length >= 2 && row[1] instanceof Number n) {
                    totalGeralPedidos += n.longValue();
                }
            }

            for (Object[] row : resultados) {
                if (row == null || row.length < 3) continue;
                StatusPedido status = (StatusPedido) row[0];
                long qtd = row[1] instanceof Number n ? n.longValue() : 0L;
                BigDecimal valor = extrairBigDecimal(row[2]);

                double pct = totalGeralPedidos > 0
                        ? ((double) qtd / totalGeralPedidos) * 100.0
                        : 0.0;
                pct = Math.round(pct * 100.0) / 100.0;

                dtos.add(new VendasPorStatusDTO(status, qtd, valor, pct));
            }
        }
        return dtos;
    }

    private Periodo normalizarPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        LocalDateTime inicioNorm = inicio != null ? inicio : LocalDate.now().atStartOfDay();
        LocalDateTime fimNorm = fim != null ? fim : LocalDate.now().atTime(LocalTime.MAX);

        if (inicioNorm.isAfter(fimNorm)) {
            throw new NegocioException("A data inicial não pode ser posterior à data final.");
        }

        return new Periodo(inicioNorm, fimNorm);
    }

    private BigDecimal calcularTicketMedio(BigDecimal totalFaturado, long totalPedidos) {
        if (totalFaturado == null || totalPedidos <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return totalFaturado.divide(BigDecimal.valueOf(totalPedidos), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal extrairBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        if (obj instanceof BigDecimal bd) return bd;
        if (obj instanceof Number num) return BigDecimal.valueOf(num.doubleValue());
        return BigDecimal.ZERO;
    }

    private record Periodo(LocalDateTime inicio, LocalDateTime fim) {}

    private record ItemBruto(Long produtoId, String nomeProduto, String categoriaNome, Long quantidadeVendida, BigDecimal valorTotal) {}
}
