package com.restaurante.pdv.event;

import com.restaurante.pdv.event.listener.NotificacaoComprovanteListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários do NotificacaoComprovanteListener")
class NotificacaoComprovanteListenerTest {

    private NotificacaoComprovanteListener listener;

    @BeforeEach
    void setUp() {
        listener = new NotificacaoComprovanteListener();
    }

    @Test
    @DisplayName("Deve gerar recibo digital com senha formatada e dados do cliente")
    void deveGerarReciboDigitalComSucesso() {
        LocalDateTime agora = LocalDateTime.of(2026, 11, 23, 19, 30, 0);
        PedidoCriadoEvent event = new PedidoCriadoEvent(
                10L,
                42,
                agora,
                new BigDecimal("48.50"),
                "Isabelly Caroline",
                "111.222.333-44"
        );

        listener.onPedidoCriado(event);

        assertTrue(listener.isComprovanteGerado(10L));
        String comprovante = listener.getComprovante(10L);
        assertNotNull(comprovante);
        assertTrue(comprovante.contains("ANTUNES LANCHES"));
        assertTrue(comprovante.contains("Pedido: #10"));
        assertTrue(comprovante.contains("Senha: #042"));
        assertTrue(comprovante.contains("Total: R$ 48.50"));
        assertTrue(comprovante.contains("Cliente: Isabelly Caroline"));
        assertTrue(comprovante.contains("CPF: 111.222.333-44"));
    }

    @Test
    @DisplayName("Deve gerar comprovante padrão quando cliente e CPF forem omitidos")
    void deveGerarComprovanteComDadosPadrao() {
        PedidoCriadoEvent event = new PedidoCriadoEvent(25L, 3, LocalDateTime.now());

        listener.onPedidoCriado(event);

        assertTrue(listener.isComprovanteGerado(25L));
        String comprovante = listener.getComprovante(25L);
        assertNotNull(comprovante);
        assertTrue(comprovante.contains("Senha: #003"));
        assertTrue(comprovante.contains("Cliente: Consumidor"));
        assertTrue(comprovante.contains("CPF: Não informado"));
    }

    @Test
    @DisplayName("Deve lidar graciosamente com evento nulo")
    void deveLidarComEventoNulo() {
        assertDoesNotThrow(() -> listener.onPedidoCriado(null));
    }
}
