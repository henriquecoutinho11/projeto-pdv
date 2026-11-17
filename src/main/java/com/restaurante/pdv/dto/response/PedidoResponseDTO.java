package com.restaurante.pdv.dto.response;

import com.restaurante.pdv.model.entity.FormaPagamento;
import com.restaurante.pdv.model.entity.Pedido;
import com.restaurante.pdv.model.entity.StatusPedido;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO de resposta detalhado para operações de consulta e criação de Pedidos.
 */
public class PedidoResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer numeroSenha;
    private LocalDateTime dataHora;
    private StatusPedido status;
    private FormaPagamento formaPagamento;
    private BigDecimal valorTotal;
    private String nomeCliente;
    private String cpfCliente;
    private List<ItemPedidoResponseDTO> itens = new ArrayList<>();
    private Integer quantidadeItens;

    public PedidoResponseDTO() {
    }

    public PedidoResponseDTO(Long id, Integer numeroSenha, LocalDateTime dataHora, StatusPedido status,
                             FormaPagamento formaPagamento, BigDecimal valorTotal,
                             String nomeCliente, String cpfCliente, List<ItemPedidoResponseDTO> itens) {
        this.id = id;
        this.numeroSenha = numeroSenha;
        this.dataHora = dataHora;
        this.status = status;
        this.formaPagamento = formaPagamento;
        this.valorTotal = valorTotal != null ? valorTotal : BigDecimal.ZERO;
        this.nomeCliente = nomeCliente;
        this.cpfCliente = cpfCliente;
        this.itens = itens != null ? itens : new ArrayList<>();
        this.quantidadeItens = this.itens.stream()
                .mapToInt(ItemPedidoResponseDTO::getQuantidade)
                .sum();
    }

    /**
     * Mapeia uma entidade Pedido para PedidoResponseDTO com todos os itens e adicionais discriminados.
     */
    public static PedidoResponseDTO deEntidade(Pedido pedido) {
        if (pedido == null) return null;

        List<ItemPedidoResponseDTO> itensDTO = pedido.getItens() != null
                ? pedido.getItens().stream().map(ItemPedidoResponseDTO::deEntidade).collect(Collectors.toList())
                : new ArrayList<>();

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getNumeroSenha(),
                pedido.getDataHora(),
                pedido.getStatus(),
                pedido.getFormaPagamento(),
                pedido.getValorTotal(),
                pedido.getNomeCliente(),
                pedido.getCpfCliente(),
                itensDTO
        );
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

    public List<ItemPedidoResponseDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoResponseDTO> itens) {
        this.itens = itens != null ? itens : new ArrayList<>();
        this.quantidadeItens = this.itens.stream()
                .mapToInt(ItemPedidoResponseDTO::getQuantidade)
                .sum();
    }

    public Integer getQuantidadeItens() {
        return quantidadeItens;
    }

    public void setQuantidadeItens(Integer quantidadeItens) {
        this.quantidadeItens = quantidadeItens;
    }
}
