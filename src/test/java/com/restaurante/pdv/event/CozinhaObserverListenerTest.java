package com.restaurante.pdv.event;

import com.restaurante.pdv.event.listener.CozinhaObserverListener;
import com.restaurante.pdv.model.entity.StatusPedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários do CozinhaObserverListener")
class CozinhaObserverListenerTest {

    private CozinhaObserverListener listener;

    @BeforeEach
    void setUp() {
        listener = new CozinhaObserverListener();
    }

    @Test
    @DisplayName("Deve enfileirar o pedido na fila da cozinha ao receber PedidoCriadoEvent")
    void deveEnfileirarPedidoAoCriar() {
        PedidoCriadoEvent event = new PedidoCriadoEvent(
                101L,
                42,
                LocalDateTime.now(),
                new BigDecimal("35.90"),
                "Carlos Silva",
                "123.456.789-00"
        );

        listener.onPedidoCriado(event);

        assertTrue(listener.isPedidoNaFila(101L));
        assertEquals(1, listener.getFilaPedidosCozinha().size());
    }

    @Test
    @DisplayName("Deve suportar construtor simplificado do PedidoCriadoEvent")
    void deveSuportarConstrutorSimplificado() {
        PedidoCriadoEvent event = new PedidoCriadoEvent(202L, 7, LocalDateTime.now());

        listener.onPedidoCriado(event);

        assertTrue(listener.isPedidoNaFila(202L));
        assertEquals(202L, event.getPedidoId());
        assertEquals(7, event.getNumeroSenha());
        assertNotNull(event.getDataHora());
    }

    @Test
    @DisplayName("Deve manter pedido na fila quando status mudar para EM_PREPARO")
    void deveManterPedidoNaFilaAoIniciarPreparo() {
        listener.onPedidoCriado(new PedidoCriadoEvent(303L, 12, LocalDateTime.now()));
        assertTrue(listener.isPedidoNaFila(303L));

        PedidoStatusAlteradoEvent statusEvent = new PedidoStatusAlteradoEvent(
                303L,
                12,
                StatusPedido.RECEBIDO,
                StatusPedido.EM_PREPARO,
                LocalDateTime.now()
        );

        listener.onPedidoStatusAlterado(statusEvent);

        assertTrue(listener.isPedidoNaFila(303L));
    }

    @Test
    @DisplayName("Deve remover pedido da fila quando status for alterado para PRONTO")
    void deveRemoverPedidoNaFilaQuandoPronto() {
        listener.onPedidoCriado(new PedidoCriadoEvent(404L, 15, LocalDateTime.now()));
        assertTrue(listener.isPedidoNaFila(404L));

        PedidoStatusAlteradoEvent statusEvent = new PedidoStatusAlteradoEvent(
                404L,
                15,
                StatusPedido.EM_PREPARO,
                StatusPedido.PRONTO,
                LocalDateTime.now()
        );

        listener.onPedidoStatusAlterado(statusEvent);

        assertFalse(listener.isPedidoNaFila(404L));
    }

    @Test
    @DisplayName("Deve remover pedido da fila quando cancelado ou entregue")
    void deveRemoverPedidoNaFilaQuandoCanceladoOuEntregue() {
        listener.onPedidoCriado(new PedidoCriadoEvent(501L, 1, LocalDateTime.now()));
        listener.onPedidoCriado(new PedidoCriadoEvent(502L, 2, LocalDateTime.now()));

        listener.onPedidoStatusAlterado(new PedidoStatusAlteradoEvent(
                501L, 1, StatusPedido.RECEBIDO, StatusPedido.CANCELADO, LocalDateTime.now()
        ));
        listener.onPedidoStatusAlterado(new PedidoStatusAlteradoEvent(
                502L, 2, StatusPedido.PRONTO, StatusPedido.ENTREGUE, LocalDateTime.now()
        ));

        assertFalse(listener.isPedidoNaFila(501L));
        assertFalse(listener.isPedidoNaFila(502L));
    }

    @Test
    @DisplayName("Deve lidar graciosamente com eventos nulos sem lançar exceções")
    void deveLidarComEventosNulos() {
        assertDoesNotThrow(() -> listener.onPedidoCriado(null));
        assertDoesNotThrow(() -> listener.onPedidoStatusAlterado(null));
    }
}
