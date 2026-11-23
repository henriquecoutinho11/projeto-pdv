package com.restaurante.pdv.exception;

import com.restaurante.pdv.model.entity.StatusPedido;

/**
 * Exceção de negócio lançada quando ocorre uma tentativa de transição ilegal no ciclo de vida do pedido
 * (ex: pular de RECEBIDO diretamente para ENTREGUE ou alterar status de um pedido já finalizado/cancelado).
 * Mapeada para HTTP 422 Unprocessable Entity conforme RFC 7807.
 */
public class TransicaoStatusInvalidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final StatusPedido statusAtual;
    private final StatusPedido statusDestino;

    public TransicaoStatusInvalidaException(StatusPedido statusAtual, StatusPedido statusDestino) {
        super(String.format("Transição de status inválida de '%s' para '%s'. O fluxo operacional permitido é RECEBIDO -> EM_PREPARO -> PRONTO -> ENTREGUE.",
                statusAtual, statusDestino));
        this.statusAtual = statusAtual;
        this.statusDestino = statusDestino;
    }

    public TransicaoStatusInvalidaException(String mensagem) {
        super(mensagem);
        this.statusAtual = null;
        this.statusDestino = null;
    }

    public StatusPedido getStatusAtual() {
        return statusAtual;
    }

    public StatusPedido getStatusDestino() {
        return statusDestino;
    }
}
