package com.restaurante.pdv.dto.response;

import com.restaurante.pdv.model.entity.OpcaoAdicional;
import com.restaurante.pdv.model.entity.TipoOpcao;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO de resposta contendo informações de uma Opção Adicional cadastrada.
 */
public class OpcaoAdicionalResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal precoExtra;
    private TipoOpcao tipo;
    private Boolean permiteRemocao;
    private Boolean ativo;

    public OpcaoAdicionalResponseDTO() {
    }

    public OpcaoAdicionalResponseDTO(Long id, String nome, String descricao, BigDecimal precoExtra, TipoOpcao tipo, Boolean permiteRemocao, Boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.precoExtra = precoExtra;
        this.tipo = tipo;
        this.permiteRemocao = permiteRemocao;
        this.ativo = ativo;
    }

    public static OpcaoAdicionalResponseDTO deEntidade(OpcaoAdicional entity) {
        if (entity == null) return null;
        return new OpcaoAdicionalResponseDTO(
                entity.getId(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getPrecoExtra(),
                entity.getTipo(),
                entity.getPermiteRemocao(),
                entity.getAtivo()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        this.permiteRemocao = permiteRemocao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}

