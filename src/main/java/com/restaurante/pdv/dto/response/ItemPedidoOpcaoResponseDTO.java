package com.restaurante.pdv.dto.response;

import com.restaurante.pdv.model.entity.ItemPedidoOpcao;
import com.restaurante.pdv.model.entity.TipoOpcao;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO de resposta representando uma opção ou adicional aplicado ao item do pedido.
 */
public class ItemPedidoOpcaoResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long opcaoAdicionalId;
    private String nomeOpcao;
    private BigDecimal precoExtra;
    private TipoOpcao tipo;

    public ItemPedidoOpcaoResponseDTO() {
    }

    public ItemPedidoOpcaoResponseDTO(Long id, Long opcaoAdicionalId, String nomeOpcao, BigDecimal precoExtra, TipoOpcao tipo) {
        this.id = id;
        this.opcaoAdicionalId = opcaoAdicionalId;
        this.nomeOpcao = nomeOpcao;
        this.precoExtra = precoExtra;
        this.tipo = tipo;
    }

    public static ItemPedidoOpcaoResponseDTO deEntidade(ItemPedidoOpcao entity) {
        if (entity == null) return null;
        Long opcaoId = entity.getOpcaoAdicional() != null ? entity.getOpcaoAdicional().getId() : null;
        return new ItemPedidoOpcaoResponseDTO(
                entity.getId(),
                opcaoId,
                entity.getNomeOpcao(),
                entity.getPrecoExtra(),
                entity.getTipo()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOpcaoAdicionalId() {
        return opcaoAdicionalId;
    }

    public void setOpcaoAdicionalId(Long opcaoAdicionalId) {
        this.opcaoAdicionalId = opcaoAdicionalId;
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
        this.precoExtra = precoExtra;
    }

    public TipoOpcao getTipo() {
        return tipo;
    }

    public void setTipo(TipoOpcao tipo) {
        this.tipo = tipo;
    }
}

