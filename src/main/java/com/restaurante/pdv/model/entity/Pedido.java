package com.restaurante.pdv.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidade que representa um Pedido finalizado ou em processamento no PDV.
 */
@Entity
@Table(name = "pedidos", indexes = {
    @Index(name = "idx_pedido_status", columnList = "status"),
    @Index(name = "idx_pedido_data_hora", columnList = "data_hora")
})
public class Pedido implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_senha")
    private Integer numeroSenha;

    @NotNull(message = "A data e hora do pedido são obrigatórias")
    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();

    @NotNull(message = "O status do pedido é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPedido status = StatusPedido.RECEBIDO;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", length = 30)
    private FormaPagamento formaPagamento;

    @NotNull(message = "O valor total do pedido é obrigatório")
    @DecimalMin(value = "0.00", message = "O valor total não pode ser negativo")
    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "nome_cliente", length = 100)
    private String nomeCliente;

    @Column(name = "cpf_cliente", length = 20)
    private String cpfCliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    public Pedido() {
    }

    public Pedido(Long id, Integer numeroSenha, LocalDateTime dataHora, StatusPedido status, FormaPagamento formaPagamento, BigDecimal valorTotal, String nomeCliente, String cpfCliente) {
        this.id = id;
        this.numeroSenha = numeroSenha;
        this.dataHora = dataHora != null ? dataHora : LocalDateTime.now();
        this.status = status != null ? status : StatusPedido.RECEBIDO;
        this.formaPagamento = formaPagamento;
        this.valorTotal = valorTotal != null ? valorTotal : BigDecimal.ZERO;
        this.nomeCliente = nomeCliente;
        this.cpfCliente = cpfCliente;
    }

    public void adicionarItem(ItemPedido item) {
        if (itens == null) {
            itens = new ArrayList<>();
        }
        itens.add(item);
        item.setPedido(this);
        recalcularTotal();
    }

    public void removerItem(ItemPedido item) {
        if (itens != null) {
            itens.remove(item);
            item.setPedido(null);
            recalcularTotal();
        }
    }

    public void recalcularTotal() {
        if (itens == null || itens.isEmpty()) {
            this.valorTotal = BigDecimal.ZERO;
            return;
        }
        this.valorTotal = itens.stream()
                .map(ItemPedido::getSubtotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumeroSenha() {
        return numeroSenha;
    }

    public void setNumeroSenha(Integer numeroSenha) {
        this.numeroSenha = numeroSenha;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getCpfCliente() {
        return cpfCliente;
    }

    public void setCpfCliente(String cpfCliente) {
        this.cpfCliente = cpfCliente;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens != null ? itens : new ArrayList<>();
        recalcularTotal();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return Objects.equals(id, pedido.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", numeroSenha=" + numeroSenha +
                ", dataHora=" + dataHora +
                ", status=" + status +
                ", valorTotal=" + valorTotal +
                '}';
    }
}

