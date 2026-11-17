package com.restaurante.pdv.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidade que representa um Produto do cardápio (ex: X-Burguer, Batata Frita, Coca-Cola).
 */
@Entity
@Table(name = "produtos", indexes = {
    @Index(name = "idx_produto_categoria", columnList = "categoria_id")
})
public class Produto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do produto é obrigatório")
    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @NotNull(message = "O preço é obrigatório")
    @DecimalMin(value = "0.00", inclusive = false, message = "O preço do produto deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @NotNull(message = "A quantidade em estoque é obrigatória")
    @Min(value = 0, message = "A quantidade em estoque não pode ser negativa")
    @Column(name = "quantidade_estoque", nullable = false)
    private Integer quantidadeEstoque;

    @Column(name = "imagem_url", length = 255)
    private String imagemUrl;

    @Column(nullable = false)
    private Boolean ativo = true;

    @NotNull(message = "A categoria é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "produtos_opcoes",
        joinColumns = @JoinColumn(name = "produto_id"),
        inverseJoinColumns = @JoinColumn(name = "opcao_id")
    )
    private Set<OpcaoAdicional> opcoesDisponiveis = new HashSet<>();

    public Produto() {
    }

    public Produto(Long id, String nome, String descricao, BigDecimal preco, Integer quantidadeEstoque, String imagemUrl, Boolean ativo, Categoria categoria) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
        this.imagemUrl = imagemUrl;
        this.ativo = ativo != null ? ativo : true;
        this.categoria = categoria;
    }

    public Produto(String nome, String descricao, BigDecimal preco, Integer quantidadeEstoque, Categoria categoria) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
        this.categoria = categoria;
        this.ativo = true;
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
        this.ativo = ativo != null ? ativo : true;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Set<OpcaoAdicional> getOpcoesDisponiveis() {
        return opcoesDisponiveis;
    }

    public void setOpcoesDisponiveis(Set<OpcaoAdicional> opcoesDisponiveis) {
        this.opcoesDisponiveis = opcoesDisponiveis != null ? opcoesDisponiveis : new HashSet<>();
    }

    public void adicionarOpcao(OpcaoAdicional opcao) {
        if (this.opcoesDisponiveis == null) {
            this.opcoesDisponiveis = new HashSet<>();
        }
        this.opcoesDisponiveis.add(opcao);
        if (opcao != null && opcao.getProdutos() != null) {
            opcao.getProdutos().add(this);
        }
    }

    public void removerOpcao(OpcaoAdicional opcao) {
        if (this.opcoesDisponiveis != null) {
            this.opcoesDisponiveis.remove(opcao);
            if (opcao != null && opcao.getProdutos() != null) {
                opcao.getProdutos().remove(this);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produto produto = (Produto) o;
        return Objects.equals(id, produto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Produto{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", preco=" + preco +
                ", quantidadeEstoque=" + quantidadeEstoque +
                ", ativo=" + ativo +
                '}';
    }
}
