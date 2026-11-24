package com.restaurante.pdv.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO com visão executiva resumida das métricas operacionais e financeiras do restaurante.
 */
public record ResumoVendasDTO(
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        BigDecimal totalFaturado,
        Long totalPedidos,
        BigDecimal ticketMedio,
        Double tempoMedioPreparoMinutos,
        List<ItemMaisVendidoDTO> topProdutosMaisVendidos,
        List<VendasPorFormaPagamentoDTO> vendasPorFormaPagamento
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
