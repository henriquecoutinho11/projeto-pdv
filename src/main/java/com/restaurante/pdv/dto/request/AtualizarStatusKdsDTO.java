package com.restaurante.pdv.dto.request;

import com.restaurante.pdv.model.entity.StatusPedido;

import java.io.Serializable;

/**
 * DTO para requisições de atualização de status do pedido na cozinha (KDS).
 * O campo status é opcional: caso omitido, o sistema avança automaticamente para a próxima etapa.
 */
public class AtualizarStatusKdsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private StatusPedido status;

    public AtualizarStatusKdsDTO() {
    }

    public AtualizarStatusKdsDTO(StatusPedido status) {
        this.status = status;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }
}
