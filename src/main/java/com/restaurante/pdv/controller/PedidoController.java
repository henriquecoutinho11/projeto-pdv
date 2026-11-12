package com.restaurante.pdv.controller;

import com.restaurante.pdv.dto.request.CustomizacaoItemDTO;
import com.restaurante.pdv.dto.response.ItemPedidoResponseDTO;
import com.restaurante.pdv.model.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para processamento de operações relacionadas a Pedidos.
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
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

