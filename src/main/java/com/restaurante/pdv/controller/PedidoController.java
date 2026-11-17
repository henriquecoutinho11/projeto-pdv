package com.restaurante.pdv.controller;

import com.restaurante.pdv.dto.request.CustomizacaoItemDTO;
import com.restaurante.pdv.dto.request.NovoPedidoDTO;
import com.restaurante.pdv.dto.response.ItemPedidoResponseDTO;
import com.restaurante.pdv.dto.response.PedidoResponseDTO;
import com.restaurante.pdv.model.entity.StatusPedido;
import com.restaurante.pdv.model.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controller REST para processamento de operações relacionadas a Pedidos e Checkout.
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /**
     * Processa a criação e fechamento transacional de um novo pedido.
     * Retorna código HTTP 201 Created com o recurso criado e status inicial RECEBIDO.
     */
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criarPedido(@Valid @RequestBody NovoPedidoDTO dto) {
        PedidoResponseDTO response = pedidoService.processarNovoPedido(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    /**
     * Retorna a lista de todos os pedidos realizados.
     */
    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    /**
     * Busca um pedido por ID com todos os detalhes discriminados.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    /**
     * Retorna pedidos filtrados por status operacional.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PedidoResponseDTO>> buscarPorStatus(@PathVariable StatusPedido status) {
        return ResponseEntity.ok(pedidoService.buscarPorStatus(status));
    }

    /**
     * Endpoint para cálculo e pré-visualização de um item customizado antes da inclusão no pedido/carrinho.
     */
    @PostMapping("/calcular-item")
    public ResponseEntity<ItemPedidoResponseDTO> calcularItem(@Valid @RequestBody CustomizacaoItemDTO dto) {
        ItemPedidoResponseDTO response = pedidoService.calcularItem(dto);
        return ResponseEntity.ok(response);
    }
}


