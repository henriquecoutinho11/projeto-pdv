package com.restaurante.pdv.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade que representa uma cobrança instantânea gerada via PIX (EMV / BR Code).
 * Possui controle de expiração de 5 minutos e status de liquidação.
 */
@Entity
@Table(name = "cobrancas_pix", indexes = {
    @Index(name = "idx_cobranca_pix_pedido", columnList = "pedido_id"),
    @Index(name = "idx_cobranca_pix_txid", columnList = "txid", unique = true)
})
public class CobrancaPix implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O pedido associado é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @NotBlank(message = "O identificador da transação (txId) é obrigatório")
    @Column(name = "txid", nullable = false, length = 100, unique = true)
    private String txId;

    @NotBlank(message = "A chave PIX é obrigatória")
    @Column(name = "chave_pix", nullable = false, length = 100)
    private String chavePix;

    @NotNull(message = "O valor da cobrança é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor da cobrança deve ser maior que zero")
    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Lob
    @Column(name = "payload_emv", columnDefinition = "TEXT", nullable = false)
    private String payloadEmv;

    @Lob
    @Column(name = "qr_code_base64", columnDefinition = "TEXT", nullable = false)
    private String qrCodeBase64;

    @NotNull(message = "A data de criação é obrigatória")
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @NotNull(message = "A data de expiração é obrigatória")
    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @NotNull(message = "O flag de pagamento é obrigatório")
    @Column(name = "pago", nullable = false)
    private Boolean pago = Boolean.FALSE;

    @Column(name = "data_hora_pagamento")
    private LocalDateTime dataHoraPagamento;

    public CobrancaPix() {
    }

    public CobrancaPix(Pedido pedido, String txId, String chavePix, BigDecimal valor,
                       String payloadEmv, String qrCodeBase64, LocalDateTime dataCriacao,
                       LocalDateTime dataExpiracao) {
        this.pedido = pedido;
        this.txId = txId;
        this.chavePix = chavePix;
        this.valor = valor;
        this.payloadEmv = payloadEmv;
        this.qrCodeBase64 = qrCodeBase64;
        this.dataCriacao = dataCriacao != null ? dataCriacao : LocalDateTime.now();
        this.dataExpiracao = dataExpiracao;
        this.pago = Boolean.FALSE;
    }

    /**
     * Verifica se a cobrança excedeu a janela de validade (5 minutos).
     */
    public boolean isExpirada() {
        return LocalDateTime.now().isAfter(dataExpiracao);
    }

    /**
     * Informa se a cobrança ainda está elegível para recebimento.
     */
    public boolean isAtiva() {
        return !Boolean.TRUE.equals(pago) && !isExpirada();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getChavePix() {
        return chavePix;
    }

    public void setChavePix(String chavePix) {
        this.chavePix = chavePix;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getPayloadEmv() {
        return payloadEmv;
    }

    public void setPayloadEmv(String payloadEmv) {
        this.payloadEmv = payloadEmv;
    }

    public String getQrCodeBase64() {
        return qrCodeBase64;
    }

    public void setQrCodeBase64(String qrCodeBase64) {
        this.qrCodeBase64 = qrCodeBase64;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }

    public void setDataExpiracao(LocalDateTime dataExpiracao) {
        this.dataExpiracao = dataExpiracao;
    }

    public Boolean getPago() {
        return pago;
    }

    public boolean isPago() {
        return Boolean.TRUE.equals(pago);
    }

    public void setPago(Boolean pago) {
        this.pago = pago != null ? pago : Boolean.FALSE;
    }

    public LocalDateTime getDataHoraPagamento() {
        return dataHoraPagamento;
    }

    public void setDataHoraPagamento(LocalDateTime dataHoraPagamento) {
        this.dataHoraPagamento = dataHoraPagamento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CobrancaPix that = (CobrancaPix) o;
        return Objects.equals(id, that.id) || Objects.equals(txId, that.txId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, txId);
    }
}
