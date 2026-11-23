package com.restaurante.pdv.event.listener;

import com.restaurante.pdv.event.PedidoCriadoEvent;

/**
 * Contrato de ouvinte de eventos para geração de comprovantes e recibos digitais.
 */
public interface ComprovanteEventListener {
    void onPedidoCriado(PedidoCriadoEvent event);
}
