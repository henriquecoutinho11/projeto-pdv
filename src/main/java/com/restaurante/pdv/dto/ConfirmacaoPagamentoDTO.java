package com.restaurante.pdv.dto;

import com.restaurante.pdv.model.entity.FormaPagamento;

import java.math.BigDecimal;

/**
 * Atalho para ConfirmacaoPagamentoDTO no pacote raiz de DTOs.
 */
public class ConfirmacaoPagamentoDTO extends com.restaurante.pdv.dto.request.ConfirmacaoPagamentoDTO {

    private static final long serialVersionUID = 1L;

    public ConfirmacaoPagamentoDTO() {
        super();
    }

    public ConfirmacaoPagamentoDTO(FormaPagamento formaPagamento, BigDecimal valorPago) {
        super(formaPagamento, valorPago);
    }

    public ConfirmacaoPagamentoDTO(FormaPagamento formaPagamento, BigDecimal valorPago, String codigoTransacao) {
        super(formaPagamento, valorPago, codigoTransacao);
    }
}
