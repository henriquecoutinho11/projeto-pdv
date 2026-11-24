package com.restaurante.pdv.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO analítico para o fechamento diário do caixa com detalhamento de formas de pagamento.
 */
public record FechamentoCaixaDTO(
        LocalDate data,
        Long totalPedidos,
        BigDecimal totalFaturado,
        BigDecimal ticketMedio,
        BigDecimal totalDinheiro,
        BigDecimal totalPix,
        BigDecimal totalCartaoCredito,
        BigDecimal totalCartaoDebito,
        List<VendasPorFormaPagamentoDTO> detalhesFormasPagamento
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
