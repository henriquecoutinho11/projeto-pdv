package com.restaurante.pdv.dto.response;

import com.restaurante.pdv.model.entity.ItemPedido;
import com.restaurante.pdv.model.entity.ItemPedidoOpcao;
import com.restaurante.pdv.model.entity.TipoOpcao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de resposta contendo os detalhes do item do pedido para o display da cozinha (KDS).
 * Destaca remoções e adicionais em formato de tags legíveis para a equipe de chapeiros.
 */
public class ItemKdsResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long produtoId;
    private String produtoNome;
    private Integer quantidade;
    private String observacoes;
    private List<String> remocoes = new ArrayList<>();
    private List<String> adicionais = new ArrayList<>();
    private List<String> tags = new ArrayList<>();

    public ItemKdsResponseDTO() {
    }

    public ItemKdsResponseDTO(Long id, Long produtoId, String produtoNome, Integer quantidade,
                              String observacoes, List<String> remocoes, List<String> adicionais, List<String> tags) {
        this.id = id;
        this.produtoId = produtoId;
        this.produtoNome = produtoNome;
        this.quantidade = quantidade;
        this.observacoes = observacoes;
        this.remocoes = remocoes != null ? remocoes : new ArrayList<>();
        this.adicionais = adicionais != null ? adicionais : new ArrayList<>();
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public static ItemKdsResponseDTO deEntidade(ItemPedido item) {
        if (item == null) return null;

        Long prodId = item.getProduto() != null ? item.getProduto().getId() : null;
        String prodNome = item.getProduto() != null ? item.getProduto().getNome() : null;

        List<String> remocoes = new ArrayList<>();
        List<String> adicionais = new ArrayList<>();
        List<String> tags = new ArrayList<>();

        if (item.getOpcoes() != null) {
            for (ItemPedidoOpcao op : item.getOpcoes()) {
                if (op.getTipo() == TipoOpcao.REMOCAO) {
                    remocoes.add(op.getNomeOpcao());
                    String tagRemocao = op.getNomeOpcao().toUpperCase().startsWith("SEM ")
                            ? op.getNomeOpcao().toUpperCase()
                            : "SEM " + op.getNomeOpcao().toUpperCase();
                    tags.add(tagRemocao);
                } else if (op.getTipo() == TipoOpcao.ADICIONAL) {
                    adicionais.add(op.getNomeOpcao());
                    tags.add("+ " + op.getNomeOpcao().toUpperCase());
                } else if (op.getTipo() == TipoOpcao.TAMANHO_BATATA) {
                    tags.add(op.getNomeOpcao().toUpperCase());
                } else if (op.getTipo() == TipoOpcao.BEBIDA) {
                    tags.add(op.getNomeOpcao().toUpperCase());
                }
            }
        }

        return new ItemKdsResponseDTO(
                item.getId(),
                prodId,
                prodNome,
                item.getQuantidade(),
                item.getObservacoes(),
                remocoes,
                adicionais,
                tags
        );
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

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
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
        this.remocoes = remocoes;
    }

    public List<String> getAdicionais() {
        return adicionais;
    }

    public void setAdicionais(List<String> adicionais) {
        this.adicionais = adicionais;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
