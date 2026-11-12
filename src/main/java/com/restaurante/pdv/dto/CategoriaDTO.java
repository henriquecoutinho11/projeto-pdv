package com.restaurante.pdv.dto;

import com.restaurante.pdv.model.entity.Categoria;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

public class CategoriaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "O nome da categoria é obrigatório")
    private String nome;

    private String descricao;

    private Boolean ativo = true;

    public CategoriaDTO() {
    }

    public CategoriaDTO(Long id, String nome, String descricao, Boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.ativo = ativo != null ? ativo : true;
    }

    public static CategoriaDTO deEntidade(Categoria entity) {
        if (entity == null) return null;
        return new CategoriaDTO(
                entity.getId(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getAtivo()
        );
    }

    public Categoria paraEntidade() {
        return new Categoria(
                this.id,
                this.nome,
                this.descricao,
                this.ativo != null ? this.ativo : true
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

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
