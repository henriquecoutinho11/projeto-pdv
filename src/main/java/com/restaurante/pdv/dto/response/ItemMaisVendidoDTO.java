package com.restaurante.pdv.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO para classificação de itens mais vendidos e Curva ABC de vendas.
 */
public record ItemMaisVendidoDTO(
        Long produtoId,
        String nomeProduto,
        String categoriaNome,
        Long quantidadeVendida,
        BigDecimal valorTotal,
        Double percentualFaturamento,
        Double percentualAcumulado,
        String classificacaoAbc
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
