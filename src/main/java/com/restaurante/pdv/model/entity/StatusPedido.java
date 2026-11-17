package com.restaurante.pdv.model.entity;

/**
 * Ciclo de vida e estados de um pedido no PDV:
 * [CARRINHO] -> [RECEBIDO] -> [EM_PREPARO] -> [PRONTO] -> [ENTREGUE] (ou [CANCELADO])
 */
public enum StatusPedido {
    CARRINHO,
    RECEBIDO,
    EM_PREPARO,
    PRONTO,
    ENTREGUE,
    CANCELADO
}

