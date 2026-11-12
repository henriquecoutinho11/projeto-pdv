package com.restaurante.pdv.dto.request;

import com.restaurante.pdv.model.entity.TipoOpcao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO para criação e atualização de Opção Adicional / Insumo no PDV.
 */
public class OpcaoAdicionalRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "O nome da opção adicional é obrigatório")
    @Size(max = 100, message = "O nome da opção deve ter no máximo 100 caracteres")
    private String nome;

    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
    private String descricao;

    @NotNull(message = "O preço extra é obrigatório")
    @DecimalMin(value = "0.00", message = "O preço extra não pode ser negativo")
    private BigDecimal precoExtra;

    @NotNull(message = "O tipo da opção é obrigatório (ADICIONAL, REMOCAO, TAMANHO_BATATA, BEBIDA)")
    private TipoOpcao tipo;

    private Boolean permiteRemocao = false;

    private Boolean ativo = true;

    public OpcaoAdicionalRequestDTO() {
    }

    public OpcaoAdicionalRequestDTO(String nome, String descricao, BigDecimal precoExtra, TipoOpcao tipo, Boolean permiteRemocao, Boolean ativo) {
        this.nome = nome;
        this.descricao = descricao;
        this.precoExtra = precoExtra;
        this.tipo = tipo;
        this.permiteRemocao = permiteRemocao != null ? permiteRemocao : false;
        this.ativo = ativo != null ? ativo : true;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPrecoExtra() {
        return precoExtra;
    }

    public void setPrecoExtra(BigDecimal precoExtra) {
        this.precoExtra = precoExtra;
    }

    public TipoOpcao getTipo() {
        return tipo;
    }

    public void setTipo(TipoOpcao tipo) {
        this.tipo = tipo;
    }

    public Boolean getPermiteRemocao() {
        return permiteRemocao;
    }

    public void setPermiteRemocao(Boolean permiteRemocao) {
        this.permiteRemocao = permiteRemocao != null ? permiteRemocao : false;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo != null ? ativo : true;
    }
}

