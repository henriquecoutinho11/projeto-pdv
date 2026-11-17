package com.restaurante.pdv.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidade que representa uma opção customizável de produto/lanche/combo (insumos, adicionais pagos, remoções e itens de combo).
 */
@Entity
@Table(name = "opcoes_adicionais", indexes = {
    @Index(name = "idx_opcao_tipo", columnList = "tipo"),
    @Index(name = "idx_opcao_ativo", columnList = "ativo")
})
public class OpcaoAdicional implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da opção adicional é obrigatório")
    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @NotNull(message = "O preço extra é obrigatório")
    @DecimalMin(value = "0.00", message = "O preço extra não pode ser negativo")
    @Column(name = "preco_extra", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoExtra = BigDecimal.ZERO;

    @NotNull(message = "O tipo da opção é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoOpcao tipo;

    @Column(name = "permite_remocao", nullable = false)
    private Boolean permiteRemocao = false;

    @Column(nullable = false)
    private Boolean ativo = true;

    @JsonIgnore
    @ManyToMany(mappedBy = "opcoesDisponiveis")
    private Set<Produto> produtos = new HashSet<>();

    public OpcaoAdicional() {
    }

    public OpcaoAdicional(Long id, String nome, String descricao, BigDecimal precoExtra, TipoOpcao tipo, Boolean permiteRemocao, Boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.precoExtra = precoExtra != null ? precoExtra : BigDecimal.ZERO;
        this.tipo = tipo;
        this.permiteRemocao = permiteRemocao != null ? permiteRemocao : false;
        this.ativo = ativo != null ? ativo : true;
    }

    public OpcaoAdicional(String nome, String descricao, BigDecimal precoExtra, TipoOpcao tipo, Boolean permiteRemocao, Boolean ativo) {
        this.nome = nome;
        this.descricao = descricao;
        this.precoExtra = precoExtra != null ? precoExtra : BigDecimal.ZERO;
        this.tipo = tipo;
        this.permiteRemocao = permiteRemocao != null ? permiteRemocao : false;
        this.ativo = ativo != null ? ativo : true;
    }

    public OpcaoAdicional(String nome, BigDecimal precoExtra, TipoOpcao tipo) {
        this.nome = nome;
        this.precoExtra = precoExtra != null ? precoExtra : BigDecimal.ZERO;
        this.tipo = tipo;
        this.permiteRemocao = tipo == TipoOpcao.REMOCAO;
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

    public BigDecimal getPrecoExtra() {
        return precoExtra;
    }

    public void setPrecoExtra(BigDecimal precoExtra) {
        this.precoExtra = precoExtra != null ? precoExtra : BigDecimal.ZERO;
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

    public Set<Produto> getProdutos() {
        return produtos;
    }

    public void setProdutos(Set<Produto> produtos) {
        this.produtos = produtos != null ? produtos : new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpcaoAdicional that = (OpcaoAdicional) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OpcaoAdicional{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", precoExtra=" + precoExtra +
                ", tipo=" + tipo +
                ", permiteRemocao=" + permiteRemocao +
                ", ativo=" + ativo +
                '}';
    }
}
