package com.restaurante.pdv.dto.response;

import com.restaurante.pdv.model.entity.StatusPedido;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO com a consolidação de pedidos agrupados por status operacional.
 */
public record VendasPorStatusDTO(
        StatusPedido status,
        Long quantidadePedidos,
        BigDecimal valorTotal,
        Double percentual
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
