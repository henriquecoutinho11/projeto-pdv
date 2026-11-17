package com.restaurante.pdv.model.entity;

/**
 * Define o tipo ou categoria da opção customizável de um produto ou combo fast-food (McDonald's Style).
 */
public enum TipoOpcao {
    /**
     * Insumos extras pagos (ex: +Bacon, +Cheddar, +Molho Extra).
     */
    ADICIONAL,

    /**
     * Ingredientes padrão do lanche que o cliente pode solicitar a remoção (ex: "Sem cebola", "Sem picles").
     */
    REMOCAO,

    /**
     * Opções de tamanho para acompanhamentos em combos (ex: Batata Média, Batata Grande).
     */
    TAMANHO_BATATA,

    /**
     * Opções de bebida elegíveis em combos (ex: Refrigerante Lata, Suco Natural, Água).
     */
    BEBIDA
}

