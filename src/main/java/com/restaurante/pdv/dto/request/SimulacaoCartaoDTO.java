package com.restaurante.pdv.dto.request;

import com.restaurante.pdv.model.entity.FormaPagamento;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * DTO para simulação de transação com terminal de cartão (PIN Pad / Maquininha touch).
 */
public class SimulacaoCartaoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "A modalidade do cartão é obrigatória (CARTAO_CREDITO ou CARTAO_DEBITO)")
    private FormaPagamento formaPagamento;

    private String bandeira;
    private String tipoOperacao; // APROXIMACAO, INSERCAO

    public SimulacaoCartaoDTO() {
    }

    public SimulacaoCartaoDTO(FormaPagamento formaPagamento, String bandeira) {
        this.formaPagamento = formaPagamento;
        this.bandeira = bandeira;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public String getBandeira() {
        return bandeira;
    }

    public void setBandeira(String bandeira) {
        this.bandeira = bandeira;
    }

    public String getTipoOperacao() {
        return tipoOperacao;
    }

    public void setTipoOperacao(String tipoOperacao) {
        this.tipoOperacao = tipoOperacao;
    }
}
