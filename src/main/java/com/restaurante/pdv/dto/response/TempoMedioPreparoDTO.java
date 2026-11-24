package com.restaurante.pdv.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO com métricas analíticas sobre o tempo de preparo de pedidos na cozinha (KDS).
 */
public record TempoMedioPreparoDTO(
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        Long totalPedidosAvaliados,
        Double tempoMedioMinutos,
        Long tempoMinimoMinutos,
        Long tempoMaximoMinutos
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
