package com.restaurante.pdv.event;

import com.restaurante.pdv.event.listener.EstoqueEventListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Testes Unitários do EstoqueEventListener")
class EstoqueEventListenerTest {

    @Test
    @DisplayName("Deve processar evento de pedido criado para auditoria sem erros")
    void deveProcessarEventoSemErros() {
        EstoqueEventListener listener = new EstoqueEventListener();
        PedidoCriadoEvent event = new PedidoCriadoEvent(
                1L,
                1,
                LocalDateTime.now(),
                new BigDecimal("22.90"),
                "Henrique Coutinho",
                null
        );

        assertDoesNotThrow(() -> listener.onPedidoCriado(event));
        assertDoesNotThrow(() -> listener.onPedidoCriado(null));
    }
}
