package com.restaurante.pdv.dto.request;

import com.restaurante.pdv.model.entity.FormaPagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO de entrada para confirmação/notificação de pagamento de pedidos (PIX, Cartão, Dinheiro).
 */
public class ConfirmacaoPagamentoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "A forma de pagamento é obrigatória")
    private FormaPagamento formaPagamento;

    @NotNull(message = "O valor pago é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor pago deve ser maior que zero")
    private BigDecimal valorPago;

    private String codigoTransacao;
    private String bandeira;
    private String nsu;
    private String txId;

    public ConfirmacaoPagamentoDTO() {
    }

    public ConfirmacaoPagamentoDTO(FormaPagamento formaPagamento, BigDecimal valorPago) {
        this.formaPagamento = formaPagamento;
        this.valorPago = valorPago;
    }

    public ConfirmacaoPagamentoDTO(FormaPagamento formaPagamento, BigDecimal valorPago, String codigoTransacao) {
        this.formaPagamento = formaPagamento;
        this.valorPago = valorPago;
        this.codigoTransacao = codigoTransacao;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public BigDecimal getValorPago() {
        return valorPago;
    }

    public void setValorPago(BigDecimal valorPago) {
        this.valorPago = valorPago;
    }

    public String getCodigoTransacao() {
        return codigoTransacao;
    }

    public void setCodigoTransacao(String codigoTransacao) {
        this.codigoTransacao = codigoTransacao;
    }

    public String getBandeira() {
        return bandeira;
    }

    public void setBandeira(String bandeira) {
        this.bandeira = bandeira;
    }

    public String getNsu() {
        return nsu;
    }

    public void setNsu(String nsu) {
        this.nsu = nsu;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }
}
