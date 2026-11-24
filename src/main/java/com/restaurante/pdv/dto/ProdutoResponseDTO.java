package com.restaurante.pdv.dto;

import com.restaurante.pdv.model.entity.Produto;
import java.io.Serializable;
import java.math.BigDecimal;

public class ProdutoResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer quantidadeEstoque;
    private String imagemUrl;
    private Boolean ativo;
    private Long categoriaId;
    private String categoriaNome;
    private Boolean estoqueBaixo;
    private String badge;
    private Boolean esgotado;
    private Boolean disponivel;

    public ProdutoResponseDTO() {
    }

    public ProdutoResponseDTO(Long id, String nome, String descricao, BigDecimal preco, Integer quantidadeEstoque, String imagemUrl, Boolean ativo, Long categoriaId, String categoriaNome) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
        this.imagemUrl = imagemUrl;
        this.ativo = ativo;
        this.categoriaId = categoriaId;
        this.categoriaNome = categoriaNome;
    }

    public static ProdutoResponseDTO deEntidade(Produto entity) {
        if (entity == null) return null;
        Long catId = entity.getCategoria() != null ? entity.getCategoria().getId() : null;
        String catNome = entity.getCategoria() != null ? entity.getCategoria().getNome() : null;

        ProdutoResponseDTO dto = new ProdutoResponseDTO(
                entity.getId(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getPreco(),
                entity.getQuantidadeEstoque(),
                entity.getImagemUrl(),
                entity.getAtivo(),
                catId,
                catNome
        );
        dto.setEstoqueBaixo(entity.isEstoqueBaixo());
        dto.setEsgotado(entity.getQuantidadeEstoque() == null || entity.getQuantidadeEstoque() <= 0);
        dto.setDisponivel(Boolean.TRUE.equals(entity.getAtivo()) && !dto.isEsgotado());

        // Atribuição de Badges de Destaque Fast-Food
        if (dto.isEsgotado()) {
            dto.setBadge("Esgotado");
        } else if (entity.getNome() != null && entity.getNome().toLowerCase().contains("smash")) {
            dto.setBadge("Mais Vendido");
        } else if (entity.getNome() != null && entity.getNome().toLowerCase().contains("combo")) {
            dto.setBadge("Oferta do Dia");
        } else if (entity.getNome() != null && (entity.getNome().toLowerCase().contains("tradicional") || entity.getNome().toLowerCase().contains("novo"))) {
            dto.setBadge("Novo");
        }

        return dto;
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

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Integer getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(Integer quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoriaNome() {
        return categoriaNome;
    }

    public void setCategoriaNome(String categoriaNome) {
        this.categoriaNome = categoriaNome;
    }

    public Boolean getEstoqueBaixo() {
        return estoqueBaixo;
    }

    public void setEstoqueBaixo(Boolean estoqueBaixo) {
        this.estoqueBaixo = estoqueBaixo;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public Boolean getEsgotado() {
        return esgotado != null ? esgotado : (quantidadeEstoque == null || quantidadeEstoque <= 0);
    }

    public boolean isEsgotado() {
        return Boolean.TRUE.equals(getEsgotado());
    }

    public void setEsgotado(Boolean esgotado) {
        this.esgotado = esgotado;
    }

    public Boolean getDisponivel() {
        return disponivel != null ? disponivel : (Boolean.TRUE.equals(ativo) && !isEsgotado());
    }

    public boolean isDisponivel() {
        return Boolean.TRUE.equals(getDisponivel());
    }

    public void setDisponivel(Boolean disponivel) {
        this.disponivel = disponivel;
    }
}
