package com.restaurante.pdv.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidade que registra uma opção ou adicional aplicado a um item de pedido específico.
 */
@Entity
@Table(name = "itens_pedido_opcoes", indexes = {
    @Index(name = "idx_item_opcao_item", columnList = "item_pedido_id")
})
public class ItemPedidoOpcao implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_pedido_id", nullable = false)
    private ItemPedido itemPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opcao_adicional_id")
    private OpcaoAdicional opcaoAdicional;

    @NotBlank(message = "O nome da opção é obrigatório")
    @Column(name = "nome_opcao", nullable = false, length = 100)
    private String nomeOpcao;

    @NotNull(message = "O preço extra da opção é obrigatório")
    @DecimalMin(value = "0.00", message = "O preço extra não pode ser negativo")
    @Column(name = "preco_extra", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoExtra = BigDecimal.ZERO;

    @NotNull(message = "O tipo da opção é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoOpcao tipo;

    public ItemPedidoOpcao() {
    }

    public ItemPedidoOpcao(OpcaoAdicional opcaoAdicional, String nomeOpcao, BigDecimal precoExtra, TipoOpcao tipo) {
        this.opcaoAdicional = opcaoAdicional;
        this.nomeOpcao = nomeOpcao;
        this.precoExtra = precoExtra != null ? precoExtra : BigDecimal.ZERO;
        this.tipo = tipo;
    }

    public ItemPedidoOpcao(String nomeOpcao, BigDecimal precoExtra, TipoOpcao tipo) {
        this.nomeOpcao = nomeOpcao;
        this.precoExtra = precoExtra != null ? precoExtra : BigDecimal.ZERO;
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ItemPedido getItemPedido() {
        return itemPedido;
    }

    public void setItemPedido(ItemPedido itemPedido) {
        this.itemPedido = itemPedido;
    }

    public OpcaoAdicional getOpcaoAdicional() {
        return opcaoAdicional;
    }

    public void setOpcaoAdicional(OpcaoAdicional opcaoAdicional) {
        this.opcaoAdicional = opcaoAdicional;
    }

    public String getNomeOpcao() {
        return nomeOpcao;
    }

    public void setNomeOpcao(String nomeOpcao) {
        this.nomeOpcao = nomeOpcao;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPedidoOpcao that = (ItemPedidoOpcao) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ItemPedidoOpcao{" +
                "id=" + id +
                ", nomeOpcao='" + nomeOpcao + '\'' +
                ", precoExtra=" + precoExtra +
                ", tipo=" + tipo +
                '}';
    }
}

