package com.restaurante.pdv.dto.request;

import com.restaurante.pdv.model.entity.FormaPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de entrada para submissão e processamento transacional de um novo pedido no PDV/Totem.
 * Contém a lista de itens customizados do carrinho, a forma de pagamento e identificação opcional do cliente.
 */
public class NovoPedidoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "O pedido deve conter pelo menos um item.")
    @Valid
    private List<CustomizacaoItemDTO> itens = new ArrayList<>();

    private FormaPagamento formaPagamento;

    private String nomeCliente;

    private String cpfCliente;

    public NovoPedidoDTO() {
    }

    public NovoPedidoDTO(List<CustomizacaoItemDTO> itens) {
        this.itens = itens != null ? itens : new ArrayList<>();
    }

    public NovoPedidoDTO(List<CustomizacaoItemDTO> itens, FormaPagamento formaPagamento, String nomeCliente, String cpfCliente) {
        this.itens = itens != null ? itens : new ArrayList<>();
        this.formaPagamento = formaPagamento;
        this.nomeCliente = nomeCliente;
        this.cpfCliente = cpfCliente;
    }

    public List<CustomizacaoItemDTO> getItens() {
        return itens;
    }

    public void setItens(List<CustomizacaoItemDTO> itens) {
        this.itens = itens != null ? itens : new ArrayList<>();
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
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
}
