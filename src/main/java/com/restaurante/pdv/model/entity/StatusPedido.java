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
    CANCELADO;

    /**
     * Valida se a transição a partir do estado atual para o novo status é permitida
     * pelas regras da máquina de estados do KDS / PDV.
     *
     * @param novoStatus status de destino desejado.
     * @return true se a transição é válida; false caso contrário.
     */
    public boolean podeTransitarPara(StatusPedido novoStatus) {
        if (novoStatus == null || this == novoStatus) {
            return false;
        }

        return switch (this) {
            case CARRINHO -> novoStatus == RECEBIDO || novoStatus == CANCELADO;
            case RECEBIDO -> novoStatus == EM_PREPARO || novoStatus == CANCELADO;
            case EM_PREPARO -> novoStatus == PRONTO || novoStatus == CANCELADO;
            case PRONTO -> novoStatus == ENTREGUE || novoStatus == CANCELADO;
            case ENTREGUE, CANCELADO -> false;
        };
    }

    /**
     * Retorna a próxima etapa operacional válida no fluxo da cozinha (KDS).
     *
     * @return próximo StatusPedido ou null caso não haja próximo estágio (ex: ENTREGUE ou CANCELADO).
     */
    public StatusPedido proximoStatus() {
        return switch (this) {
            case CARRINHO -> RECEBIDO;
            case RECEBIDO -> EM_PREPARO;
            case EM_PREPARO -> PRONTO;
            case PRONTO -> ENTREGUE;
            case ENTREGUE, CANCELADO -> null;
        };
    }
}
