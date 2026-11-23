package com.restaurante.pdv.event.listener;

import com.restaurante.pdv.event.PedidoCriadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener reativo responsável pela geração da ficha de impressão / recibo digital / comprovante
 * quando um novo pedido é registrado.
 */
@Component
public class NotificacaoComprovanteListener implements ComprovanteEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoComprovanteListener.class);

    private final Map<Long, String> comprovantesGerados = new ConcurrentHashMap<>();

    @Override
    @EventListener
    public void onPedidoCriado(PedidoCriadoEvent event) {
        try {
            if (event == null || event.getPedidoId() == null) {
                return;
            }
            String senhaFormatada = String.format("#%03d", event.getNumeroSenha() != null ? event.getNumeroSenha() : 0);
            String recibo = String.format("""
                    ========================================
                               ANTUNES LANCHES
                    ========================================
                    Pedido: #%d
                    Senha: %s
                    Data/Hora: %s
                    Cliente: %s
                    CPF: %s
                    Total: R$ %s
                    ========================================
                    """,
                    event.getPedidoId(),
                    senhaFormatada,
                    event.getDataHora(),
                    event.getNomeCliente() != null ? event.getNomeCliente() : "Consumidor",
                    event.getCpfCliente() != null ? event.getCpfCliente() : "Não informado",
                    event.getValorTotal() != null ? event.getValorTotal() : "0.00"
            );

            comprovantesGerados.put(event.getPedidoId(), recibo);

            log.info("[COMPROVANTE] Recibo digital gerado com sucesso para Pedido #{} - Senha {}",
                    event.getPedidoId(), senhaFormatada);
        } catch (Exception e) {
            log.error("[COMPROVANTE] Erro ao gerar comprovante para Pedido #{}: {}",
                    event != null ? event.getPedidoId() : null, e.getMessage(), e);
        }
    }

    public String getComprovante(Long pedidoId) {
        return comprovantesGerados.get(pedidoId);
    }

    public boolean isComprovanteGerado(Long pedidoId) {
        return comprovantesGerados.containsKey(pedidoId);
    }

    public void limparComprovantes() {
        comprovantesGerados.clear();
    }
}
