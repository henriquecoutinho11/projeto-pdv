package com.restaurante.pdv.controller;

import com.restaurante.pdv.dto.request.AtualizarStatusKdsDTO;
import com.restaurante.pdv.dto.response.PedidoKdsResponseDTO;
import com.restaurante.pdv.model.entity.StatusPedido;
import com.restaurante.pdv.model.service.PedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para o painel operacional da cozinha (KDS - Kitchen Display System).
 * Gerencia o ciclo de vida e a máquina de estados dos pedidos em preparo.
 */
@RestController
@RequestMapping("/api/kds")
public class KdsController {

    private final PedidoService pedidoService;

    public KdsController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /**
     * Lista pedidos ativos para a cozinha nos estados RECEBIDO e EM_PREPARO,
     * ordenados por tempo de espera (mais antigos primeiro).
     * Inclui itens discriminados com tags de personalização ("SEM CEBOLA", "+ BACON")
     * e cálculo de tempo decorrido para controle de SLA.
     */
    @GetMapping("/pedidos-ativos")
    public ResponseEntity<List<PedidoKdsResponseDTO>> listarPedidosAtivos() {
        return ResponseEntity.ok(pedidoService.listarPedidosAtivosKds());
    }

    /**
     * Lista pedidos prontos aguardando retirada/expedição no balcão (status PRONTO).
     */
    @GetMapping("/pedidos-prontos")
    public ResponseEntity<List<PedidoKdsResponseDTO>> listarPedidosProntos() {
        return ResponseEntity.ok(pedidoService.listarPedidosProntosKds());
    }

    /**
     * Atualiza ou avança o status operacional do pedido na cozinha.
     * Caso o novo status não seja explicitado no corpo ou parâmetro, o sistema avança
     * automaticamente para o próximo estágio natural (RECEBIDO -> EM_PREPARO -> PRONTO -> ENTREGUE).
     *
     * @param id identificador do pedido.
     * @param body corpo da requisição opcional contendo o status desejado.
     * @param statusParam query parameter opcional com o status desejado.
     * @return DTO com os dados do pedido atualizado no KDS.
     */
    @PatchMapping("/pedidos/{id}/status")
    public ResponseEntity<PedidoKdsResponseDTO> atualizarStatus(
            @PathVariable Long id,
            @RequestBody(required = false) AtualizarStatusKdsDTO body,
            @RequestParam(required = false) StatusPedido statusParam) {

        StatusPedido novoStatus = null;
        if (body != null && body.getStatus() != null) {
            novoStatus = body.getStatus();
        } else if (statusParam != null) {
            novoStatus = statusParam;
        }

        PedidoKdsResponseDTO response = pedidoService.atualizarStatusKds(id, novoStatus);
        return ResponseEntity.ok(response);
    }
}
