package com.restaurante.pdv.dto.response;

import com.restaurante.pdv.model.entity.FormaPagamento;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO com a consolidação de vendas agrupadas por forma de pagamento.
 */
public record VendasPorFormaPagamentoDTO(
        FormaPagamento formaPagamento,
        String descricao,
        Long quantidadePedidos,
        BigDecimal totalFaturado,
        Double percentual
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
