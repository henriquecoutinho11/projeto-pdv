package com.restaurante.pdv.dto.request;

import com.restaurante.pdv.model.entity.TipoConsumo;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * DTO para captura e registro da escolha da modalidade de consumo no totem (Comer Aqui vs Para Viagem).
 */
public record ModalidadeConsumoDTO(
        @NotNull(message = "A modalidade de consumo é obrigatória.")
        TipoConsumo tipoConsumo
) implements Serializable {
}
