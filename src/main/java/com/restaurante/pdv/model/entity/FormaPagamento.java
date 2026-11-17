package com.restaurante.pdv.model.entity;

/**
 * Modalidades de pagamento aceitas no PDV.
 */
public enum FormaPagamento {
    PIX("PIX"),
    CARTAO_CREDITO("Cartão de Crédito"),
    CARTAO_DEBITO("Cartão de Débito"),
    DINHEIRO("Dinheiro");

    private final String descricao;

    FormaPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isPix() {
        return this == PIX;
    }

    public boolean isCartao() {
        return this == CARTAO_CREDITO || this == CARTAO_DEBITO;
    }
}


