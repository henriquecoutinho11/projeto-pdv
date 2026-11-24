package com.restaurante.pdv.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO consolidado do relatório analítico de faturamento e desempenho comercial.
 */
public record RelatorioFaturamentoDTO(
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        Long totalPedidos,
        BigDecimal totalFaturado,
        BigDecimal ticketMedio,
        List<VendasPorFormaPagamentoDTO> vendasPorFormaPagamento,
        List<VendasPorStatusDTO> vendasPorStatus
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
