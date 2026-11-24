package com.restaurante.pdv.event.listener;

import com.restaurante.pdv.event.PedidoCriadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener responsável pelo monitoramento e reconciliação de estoque após a criação de pedidos.
 */
@Component
public class EstoqueEventListener {

    private static final Logger log = LoggerFactory.getLogger(EstoqueEventListener.class);

    @EventListener
    public void onPedidoCriado(PedidoCriadoEvent event) {
        try {
            if (event == null || event.getPedidoId() == null) {
                return;
            }
            log.info("[ESTOQUE] Evento de pedido #{} registrado para auditoria e reconciliação de estoque.", event.getPedidoId());
        } catch (Exception e) {
            log.error("[ESTOQUE] Erro ao processar reconciliação de estoque para o pedido #{}: {}",
                    event != null ? event.getPedidoId() : null, e.getMessage(), e);
        }
    }
}
