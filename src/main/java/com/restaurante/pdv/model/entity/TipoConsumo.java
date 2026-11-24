package com.restaurante.pdv.model.entity;

/**
 * Modalidade de consumo selecionada pelo cliente no início do autoatendimento no Totem.
 */
public enum TipoConsumo {
    COMER_AQUI("Para Comer Aqui"),
    PARA_VIAGEM("Para Viagem");

    private final String descricao;

    TipoConsumo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
