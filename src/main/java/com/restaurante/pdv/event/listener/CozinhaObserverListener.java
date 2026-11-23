package com.restaurante.pdv.event.listener;

import com.restaurante.pdv.event.PedidoCriadoEvent;
import com.restaurante.pdv.event.PedidoStatusAlteradoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Listener reativo responsável por enfileirar o pedido para a cozinha (KDS - Kitchen Display System)
 * ao receber eventos de ciclo de vida do pedido via padrão Observer.
 */
@Component
public class CozinhaObserverListener implements KdsEventListener {

    private static final Logger log = LoggerFactory.getLogger(CozinhaObserverListener.class);

    private final Set<Long> filaPedidosCozinha = Collections.synchronizedSet(new LinkedHashSet<>());

    @Override
    @EventListener
    public void onPedidoCriado(PedidoCriadoEvent event) {
        try {
            if (event == null || event.getPedidoId() == null) {
                return;
            }
            filaPedidosCozinha.add(event.getPedidoId());
            log.info("[KDS-COZINHA] Pedido #{} (Senha #{}) enfileirado na cozinha. Horário: {}",
                    event.getPedidoId(),
                    String.format("%03d", event.getNumeroSenha() != null ? event.getNumeroSenha() : 0),
                    event.getDataHora());
        } catch (Exception e) {
            log.error("[KDS-COZINHA] Erro ao processar PedidoCriadoEvent para o pedido #{}: {}",
                    event != null ? event.getPedidoId() : null, e.getMessage(), e);
        }
    }

    @Override
    @EventListener
    public void onPedidoStatusAlterado(PedidoStatusAlteradoEvent event) {
        try {
            if (event == null || event.getPedidoId() == null) {
                return;
            }
            log.info("[KDS-COZINHA] Pedido #{} (Senha #{}) teve status alterado: {} -> {}",
                    event.getPedidoId(),
                    String.format("%03d", event.getNumeroSenha() != null ? event.getNumeroSenha() : 0),
                    event.getStatusAnterior(),
                    event.getNovoStatus());

            if (event.getNovoStatus() != null) {
                switch (event.getNovoStatus()) {
                    case EM_PREPARO -> filaPedidosCozinha.add(event.getPedidoId());
                    case PRONTO, ENTREGUE, CANCELADO -> filaPedidosCozinha.remove(event.getPedidoId());
                    default -> {}
                }
            }
        } catch (Exception e) {
            log.error("[KDS-COZINHA] Erro ao processar PedidoStatusAlteradoEvent para o pedido #{}: {}",
                    event != null ? event.getPedidoId() : null, e.getMessage(), e);
        }
    }

    public Set<Long> getFilaPedidosCozinha() {
        return Collections.unmodifiableSet(filaPedidosCozinha);
    }

    public boolean isPedidoNaFila(Long pedidoId) {
        return filaPedidosCozinha.contains(pedidoId);
    }

    public void limparFila() {
        filaPedidosCozinha.clear();
    }
}
