package com.restaurante.pdv.dto.response;

import com.restaurante.pdv.model.entity.CobrancaPix;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * DTO de resposta com os dados completos da cobrança PIX gerada para exibição no totem.
 */
public class CobrancaPixResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long pedidoId;
    private String txId;
    private String chavePix;
    private BigDecimal valor;
    private String payloadEmv;
    private String qrCodeBase64;
    private String qrCodeDataUri;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataExpiracao;
    private Long segundosRestantes;
    private Boolean expirado;
    private Boolean pago;

    public CobrancaPixResponseDTO() {
    }

    public static CobrancaPixResponseDTO deEntidade(CobrancaPix cobranca) {
        if (cobranca == null) return null;

        CobrancaPixResponseDTO dto = new CobrancaPixResponseDTO();
        dto.setId(cobranca.getId());
        dto.setPedidoId(cobranca.getPedido() != null ? cobranca.getPedido().getId() : null);
        dto.setTxId(cobranca.getTxId());
        dto.setChavePix(cobranca.getChavePix());
        dto.setValor(cobranca.getValor());
        dto.setPayloadEmv(cobranca.getPayloadEmv());
        dto.setQrCodeBase64(cobranca.getQrCodeBase64());
        dto.setQrCodeDataUri("data:image/png;base64," + cobranca.getQrCodeBase64());
        dto.setDataCriacao(cobranca.getDataCriacao());
        dto.setDataExpiracao(cobranca.getDataExpiracao());

        long restantes = Duration.between(LocalDateTime.now(), cobranca.getDataExpiracao()).getSeconds();
        dto.setSegundosRestantes(Math.max(0, restantes));
        dto.setExpirado(cobranca.isExpirada());
        dto.setPago(cobranca.isPago());

        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
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

    public String getQrCodeDataUri() {
        return qrCodeDataUri;
    }

    public void setQrCodeDataUri(String qrCodeDataUri) {
        this.qrCodeDataUri = qrCodeDataUri;
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

    public Long getSegundosRestantes() {
        return segundosRestantes;
    }

    public void setSegundosRestantes(Long segundosRestantes) {
        this.segundosRestantes = segundosRestantes;
    }

    public Boolean getExpirado() {
        return expirado;
    }

    public void setExpirado(Boolean expirado) {
        this.expirado = expirado;
    }

    public Boolean getPago() {
        return pago;
    }

    public void setPago(Boolean pago) {
        this.pago = pago;
    }
}
