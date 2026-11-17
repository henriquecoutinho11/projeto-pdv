package com.restaurante.pdv.dto;

import java.util.List;

/**
 * Atalho para CustomizacaoItemDTO no pacote raiz de DTOs.
 */
public class CustomizacaoItemDTO extends com.restaurante.pdv.dto.request.CustomizacaoItemDTO {

    private static final long serialVersionUID = 1L;

    public CustomizacaoItemDTO() {
        super();
    }

    public CustomizacaoItemDTO(Long produtoId, Integer quantidade) {
        super(produtoId, quantidade);
    }

    public CustomizacaoItemDTO(Long produtoId, Integer quantidade, List<Long> adicionaisIds, List<Long> remocoesIds, String observacoes) {
        super(produtoId, quantidade, adicionaisIds, remocoesIds, observacoes);
    }
}

