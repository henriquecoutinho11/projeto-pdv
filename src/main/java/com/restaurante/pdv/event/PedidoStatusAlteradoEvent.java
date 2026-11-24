package com.restaurante.pdv.event;

import com.restaurante.pdv.model.entity.StatusPedido;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Evento imutável publicado quando o status operacional de um pedido é alterado.
 */
public record PedidoStatusAlteradoEvent(
        Long pedidoId,
        Integer numeroSenha,
        StatusPedido statusAnterior,
        StatusPedido novoStatus,
        LocalDateTime dataHora
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public Long getPedidoId() {
        return pedidoId;
    }

    public Integer getNumeroSenha() {
        return numeroSenha;
    }

    public StatusPedido getStatusAnterior() {
        return statusAnterior;
    }

    public StatusPedido getNovoStatus() {
        return novoStatus;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
