package com.restaurante.pdv.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de entrada para customização de item de pedido no Totem/PDV (McDonald's Style).
 * Permite adicionar insumos pagos (+Bacon, +Cheddar), solicitar remoções ("Sem cebola", "Sem picles"),
 * selecionar tamanho de acompanhamento e bebida em combos, e inserir observações livres.
 */
public class CustomizacaoItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "O ID do produto é obrigatório")
    private Long produtoId;

    @NotNull(message = "A quantidade é obrigatória")
    @Min(value = 1, message = "A quantidade mínima do produto deve ser 1")
    private Integer quantidade = 1;

    /**
     * IDs das opções adicionais pagas selecionadas (ex: +Bacon, +Cheddar).
     */
    private List<Long> adicionaisIds = new ArrayList<>();

    /**
     * IDs dos ingredientes padrão do lanche a serem removidos (ex: cebola, picles).
     */
    private List<Long> remocoesIds = new ArrayList<>();

    /**
     * Lista textual de nomes de ingredientes para remoção (caso enviado em texto puro pelo front-end).
     */
    private List<String> remocoes = new ArrayList<>();

    /**
     * ID da opção de tamanho de batata/acompanhamento selecionada para combos (ex: Batata Média, Batata Grande).
     */
    private Long tamanhoBatataId;

    /**
     * ID da opção de bebida selecionada para combos (ex: Coca-Cola, Suco de Laranja).
     */
    private Long bebidaId;

    /**
     * Observações adicionais do cliente para a cozinha (ex: "Carne bem passada").
     */
    private String observacoes;

    public CustomizacaoItemDTO() {
    }

    public CustomizacaoItemDTO(Long produtoId, Integer quantidade) {
        this.produtoId = produtoId;
        this.quantidade = quantidade != null ? quantidade : 1;
    }

    public CustomizacaoItemDTO(Long produtoId, Integer quantidade, List<Long> adicionaisIds, List<Long> remocoesIds, String observacoes) {
        this.produtoId = produtoId;
        this.quantidade = quantidade != null ? quantidade : 1;
        this.adicionaisIds = adicionaisIds != null ? adicionaisIds : new ArrayList<>();
        this.remocoesIds = remocoesIds != null ? remocoesIds : new ArrayList<>();
        this.observacoes = observacoes;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public List<Long> getAdicionaisIds() {
        return adicionaisIds;
    }

    public void setAdicionaisIds(List<Long> adicionaisIds) {
        this.adicionaisIds = adicionaisIds != null ? adicionaisIds : new ArrayList<>();
    }

    public List<Long> getRemocoesIds() {
        return remocoesIds;
    }

    public void setRemocoesIds(List<Long> remocoesIds) {
        this.remocoesIds = remocoesIds != null ? remocoesIds : new ArrayList<>();
    }

    public List<String> getRemocoes() {
        return remocoes;
    }

    public void setRemocoes(List<String> remocoes) {
        this.remocoes = remocoes != null ? remocoes : new ArrayList<>();
    }

    public Long getTamanhoBatataId() {
        return tamanhoBatataId;
    }

    public void setTamanhoBatataId(Long tamanhoBatataId) {
        this.tamanhoBatataId = tamanhoBatataId;
    }

    public Long getBebidaId() {
        return bebidaId;
    }

    public void setBebidaId(Long bebidaId) {
        this.bebidaId = bebidaId;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}

