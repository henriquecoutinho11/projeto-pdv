package com.restaurante.pdv.dto.response;

import com.restaurante.pdv.model.entity.ItemPedido;
import com.restaurante.pdv.model.entity.TipoOpcao;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO de resposta contendo o cálculo discriminado do item customizado (McDonald's Style):
 * subtotal = (precoBaseProduto + somaAdicionais + diferencaTamanho) * quantidade.
 */
public class ItemPedidoResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long produtoId;
    private String produtoNome;
    private BigDecimal precoBaseProduto;
    private BigDecimal somaAdicionais = BigDecimal.ZERO;
    private BigDecimal diferencaTamanho = BigDecimal.ZERO;
    private BigDecimal precoUnitario;
    private Integer quantidade;
    private BigDecimal subtotal;
    private String observacoes;
    private List<String> remocoes = new ArrayList<>();
    private List<ItemPedidoOpcaoResponseDTO> opcoes = new ArrayList<>();

    public ItemPedidoResponseDTO() {
    }

    public ItemPedidoResponseDTO(Long id, Long produtoId, String produtoNome, BigDecimal precoBaseProduto,
                                 BigDecimal somaAdicionais, BigDecimal diferencaTamanho, BigDecimal precoUnitario,
                                 Integer quantidade, BigDecimal subtotal, String observacoes,
                                 List<String> remocoes, List<ItemPedidoOpcaoResponseDTO> opcoes) {
        this.id = id;
        this.produtoId = produtoId;
        this.produtoNome = produtoNome;
        this.precoBaseProduto = precoBaseProduto;
        this.somaAdicionais = somaAdicionais != null ? somaAdicionais : BigDecimal.ZERO;
        this.diferencaTamanho = diferencaTamanho != null ? diferencaTamanho : BigDecimal.ZERO;
        this.precoUnitario = precoUnitario;
        this.quantidade = quantidade;
        this.subtotal = subtotal;
        this.observacoes = observacoes;
        this.remocoes = remocoes != null ? remocoes : new ArrayList<>();
        this.opcoes = opcoes != null ? opcoes : new ArrayList<>();
    }

    public static ItemPedidoResponseDTO deEntidade(ItemPedido item, BigDecimal somaAdicionais, BigDecimal diferencaTamanho, List<String> remocoes) {
        if (item == null) return null;

        Long prodId = item.getProduto() != null ? item.getProduto().getId() : null;
        String prodNome = item.getProduto() != null ? item.getProduto().getNome() : null;
        BigDecimal precoBase = item.getProduto() != null ? item.getProduto().getPreco() : BigDecimal.ZERO;

        List<ItemPedidoOpcaoResponseDTO> opcoesDTO = item.getOpcoes() != null
                ? item.getOpcoes().stream().map(ItemPedidoOpcaoResponseDTO::deEntidade).collect(Collectors.toList())
                : new ArrayList<>();

        return new ItemPedidoResponseDTO(
                item.getId(),
                prodId,
                prodNome,
                precoBase,
                somaAdicionais,
                diferencaTamanho,
                item.getPrecoUnitario(),
                item.getQuantidade(),
                item.getSubtotal(),
                item.getObservacoes(),
                remocoes,
                opcoesDTO
        );
    }

    public static ItemPedidoResponseDTO deEntidade(ItemPedido item) {
        if (item == null) return null;

        BigDecimal adicionais = BigDecimal.ZERO;
        BigDecimal tamanho = BigDecimal.ZERO;
        List<String> rems = new ArrayList<>();

        if (item.getOpcoes() != null) {
            for (var op : item.getOpcoes()) {
                if (op.getTipo() == TipoOpcao.TAMANHO_BATATA) {
                    tamanho = tamanho.add(op.getPrecoExtra());
                } else if (op.getTipo() == TipoOpcao.ADICIONAL || op.getTipo() == TipoOpcao.BEBIDA) {
                    adicionais = adicionais.add(op.getPrecoExtra());
                } else if (op.getTipo() == TipoOpcao.REMOCAO) {
                    rems.add(op.getNomeOpcao());
                }
            }
        }

        return deEntidade(item, adicionais, tamanho, rems);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public String getProdutoNome() {
        return produtoNome;
    }

    public void setProdutoNome(String produtoNome) {
        this.produtoNome = produtoNome;
    }

    public BigDecimal getPrecoBaseProduto() {
        return precoBaseProduto;
    }

    public void setPrecoBaseProduto(BigDecimal precoBaseProduto) {
        this.precoBaseProduto = precoBaseProduto;
    }

    public BigDecimal getSomaAdicionais() {
        return somaAdicionais;
    }

    public void setSomaAdicionais(BigDecimal somaAdicionais) {
        this.somaAdicionais = somaAdicionais;
    }

    public BigDecimal getDiferencaTamanho() {
        return diferencaTamanho;
    }

    public void setDiferencaTamanho(BigDecimal diferencaTamanho) {
        this.diferencaTamanho = diferencaTamanho;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
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

    public List<String> getRemocoes() {
        return remocoes;
    }

    public void setRemocoes(List<String> remocoes) {
        this.remocoes = remocoes != null ? remocoes : new ArrayList<>();
    }

    public List<ItemPedidoOpcaoResponseDTO> getOpcoes() {
        return opcoes;
    }

    public void setOpcoes(List<ItemPedidoOpcaoResponseDTO> opcoes) {
        this.opcoes = opcoes != null ? opcoes : new ArrayList<>();
    }
}

