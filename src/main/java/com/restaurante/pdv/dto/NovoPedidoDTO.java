package com.restaurante.pdv.dto;

import com.restaurante.pdv.dto.request.CustomizacaoItemDTO;
import com.restaurante.pdv.model.entity.FormaPagamento;

import java.util.List;

/**
 * Atalho para NovoPedidoDTO no pacote raiz de DTOs.
 */
public class NovoPedidoDTO extends com.restaurante.pdv.dto.request.NovoPedidoDTO {

    private static final long serialVersionUID = 1L;

    public NovoPedidoDTO() {
        super();
    }

    public NovoPedidoDTO(List<CustomizacaoItemDTO> itens) {
        super(itens);
    }

    public NovoPedidoDTO(List<CustomizacaoItemDTO> itens, FormaPagamento formaPagamento, String nomeCliente, String cpfCliente) {
        super(itens, formaPagamento, nomeCliente, cpfCliente);
    }
}
