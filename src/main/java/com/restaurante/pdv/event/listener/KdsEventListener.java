package com.restaurante.pdv.event.listener;

import com.restaurante.pdv.event.PedidoCriadoEvent;
import com.restaurante.pdv.event.PedidoStatusAlteradoEvent;

/**
 * Contrato de ouvinte de eventos operacionais da cozinha (KDS).
 */
public interface KdsEventListener {
    void onPedidoCriado(PedidoCriadoEvent event);
    void onPedidoStatusAlterado(PedidoStatusAlteradoEvent event);
}
