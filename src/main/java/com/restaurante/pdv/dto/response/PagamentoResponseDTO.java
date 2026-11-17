package com.restaurante.pdv.dto.response;

import com.restaurante.pdv.model.entity.FormaPagamento;
import com.restaurante.pdv.model.entity.Pedido;
import com.restaurante.pdv.model.entity.StatusPedido;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de resposta para operações de pagamento de pedidos.
 */
public class PagamentoResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long pedidoId;
    private Integer numeroSenha;
    private FormaPagamento formaPagamento;
    private BigDecimal valorPago;
    private StatusPedido statusPedido;
    private Boolean pago;
    private LocalDateTime dataHoraPagamento;
    private String codigoTransacao;
    private String mensagem;

    public PagamentoResponseDTO() {
    }

    public PagamentoResponseDTO(Long pedidoId, Integer numeroSenha, FormaPagamento formaPagamento,
                                BigDecimal valorPago, StatusPedido statusPedido, Boolean pago,
                                LocalDateTime dataHoraPagamento, String codigoTransacao, String mensagem) {
        this.pedidoId = pedidoId;
        this.numeroSenha = numeroSenha;
        this.formaPagamento = formaPagamento;
        this.valorPago = valorPago;
        this.statusPedido = statusPedido;
        this.pago = pago;
        this.dataHoraPagamento = dataHoraPagamento;
        this.codigoTransacao = codigoTransacao;
        this.mensagem = mensagem;
    }

    public static PagamentoResponseDTO dePedido(Pedido pedido, String mensagem) {
        if (pedido == null) return null;

        return new PagamentoResponseDTO(
                pedido.getId(),
                pedido.getNumeroSenha(),
                pedido.getFormaPagamento(),
                pedido.getValorTotal(),
                pedido.getStatus(),
                pedido.isPago(),
                pedido.getDataHoraPagamento(),
                pedido.getCodigoTransacao(),
                mensagem
        );
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Integer getNumeroSenha() {
        return numeroSenha;
    }

    public void setNumeroSenha(Integer numeroSenha) {
        this.numeroSenha = numeroSenha;
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

    public StatusPedido getStatusPedido() {
        return statusPedido;
    }

    public void setStatusPedido(StatusPedido statusPedido) {
        this.statusPedido = statusPedido;
    }

    public Boolean getPago() {
        return pago;
    }

    public boolean isPago() {
        return Boolean.TRUE.equals(pago);
    }

    public void setPago(Boolean pago) {
        this.pago = pago;
    }

    public LocalDateTime getDataHoraPagamento() {
        return dataHoraPagamento;
    }

    public void setDataHoraPagamento(LocalDateTime dataHoraPagamento) {
        this.dataHoraPagamento = dataHoraPagamento;
    }

    public String getCodigoTransacao() {
        return codigoTransacao;
    }

    public void setCodigoTransacao(String codigoTransacao) {
        this.codigoTransacao = codigoTransacao;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
