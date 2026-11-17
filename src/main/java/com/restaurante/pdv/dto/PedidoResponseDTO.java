package com.restaurante.pdv.dto;

import com.restaurante.pdv.dto.response.ItemPedidoResponseDTO;
import com.restaurante.pdv.model.entity.FormaPagamento;
import com.restaurante.pdv.model.entity.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Atalho para PedidoResponseDTO no pacote raiz de DTOs.
 */
public class PedidoResponseDTO extends com.restaurante.pdv.dto.response.PedidoResponseDTO {

    private static final long serialVersionUID = 1L;

    public PedidoResponseDTO() {
        super();
    }

    public PedidoResponseDTO(Long id, Integer numeroSenha, LocalDateTime dataHora, StatusPedido status,
                             FormaPagamento formaPagamento, BigDecimal valorTotal,
                             String nomeCliente, String cpfCliente, List<ItemPedidoResponseDTO> itens) {
        super(id, numeroSenha, dataHora, status, formaPagamento, valorTotal, nomeCliente, cpfCliente, itens);
    }
}
