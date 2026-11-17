package com.restaurante.pdv.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidade que representa um item pertencente a um Pedido, contendo o produto selecionado,
 * a quantidade, os adicionais escolhidos, remoções solicitadas e observações.
 */
@Entity
@Table(name = "itens_pedido", indexes = {
    @Index(name = "idx_item_pedido_pedido", columnList = "pedido_id"),
    @Index(name = "idx_item_pedido_produto", columnList = "produto_id")
})
public class ItemPedido implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @NotNull(message = "O produto do item é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @NotNull(message = "A quantidade é obrigatória")
    @Min(value = 1, message = "A quantidade mínima do item deve ser 1")
    @Column(nullable = false)
    private Integer quantidade = 1;

    @NotNull(message = "O preço unitário é obrigatório")
    @DecimalMin(value = "0.00", message = "O preço unitário não pode ser negativo")
    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario = BigDecimal.ZERO;

    @NotNull(message = "O subtotal é obrigatório")
    @DecimalMin(value = "0.00", message = "O subtotal não pode ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(length = 500)
    private String observacoes;

    @OneToMany(mappedBy = "itemPedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedidoOpcao> opcoes = new ArrayList<>();

    public ItemPedido() {
    }

    public ItemPedido(Produto produto, Integer quantidade, BigDecimal precoUnitario, BigDecimal subtotal, String observacoes) {
        this.produto = produto;
        this.quantidade = quantidade != null ? quantidade : 1;
        this.precoUnitario = precoUnitario != null ? precoUnitario : BigDecimal.ZERO;
        this.subtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        this.observacoes = observacoes;
    }

    public void adicionarOpcao(ItemPedidoOpcao opcao) {
        if (opcoes == null) {
            opcoes = new ArrayList<>();
        }
        opcoes.add(opcao);
        opcao.setItemPedido(this);
    }

    public void removerOpcao(ItemPedidoOpcao opcao) {
        if (opcoes != null) {
            opcoes.remove(opcao);
            opcao.setItemPedido(null);
        }
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

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public List<ItemPedidoOpcao> getOpcoes() {
        return opcoes;
    }

    public void setOpcoes(List<ItemPedidoOpcao> opcoes) {
        this.opcoes = opcoes != null ? opcoes : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPedido that = (ItemPedido) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ItemPedido{" +
                "id=" + id +
                ", produto=" + (produto != null ? produto.getNome() : null) +
                ", quantidade=" + quantidade +
                ", precoUnitario=" + precoUnitario +
                ", subtotal=" + subtotal +
                ", observacoes='" + observacoes + '\'' +
                '}';
    }
}

