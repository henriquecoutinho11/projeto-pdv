package com.restaurante.pdv.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento imutável publicado quando um novo pedido é criado e persistido no PDV.
 * Utilizado pelo padrão Observer para desacoplar e notificar módulos secundários (KDS, comprovantes, etc.).
 */
public record PedidoCriadoEvent(
        Long pedidoId,
        Integer numeroSenha,
        LocalDateTime dataHora,
        BigDecimal valorTotal,
        String nomeCliente,
        String cpfCliente
) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Construtor simplificado conforme especificação do CARD-07.
     */
    public PedidoCriadoEvent(Long pedidoId, Integer numeroSenha, LocalDateTime dataHora) {
        this(pedidoId, numeroSenha, dataHora, null, null, null);
    }

    public PedidoCriadoEvent(Long pedidoId, Integer numeroSenha, LocalDateTime dataHora, BigDecimal valorTotal, String nomeCliente) {
        this(pedidoId, numeroSenha, dataHora, valorTotal, nomeCliente, null);
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public Integer getNumeroSenha() {
        return numeroSenha;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public String getCpfCliente() {
        return cpfCliente;
    }
}
