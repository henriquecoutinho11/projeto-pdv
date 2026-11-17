package com.restaurante.pdv.controller;

import com.restaurante.pdv.dto.request.CustomizacaoItemDTO;
import com.restaurante.pdv.dto.response.ItemPedidoResponseDTO;
import com.restaurante.pdv.dto.response.OpcaoAdicionalResponseDTO;
import com.restaurante.pdv.model.service.OpcaoAdicionalService;
import com.restaurante.pdv.model.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para o Totem de Autoatendimento do Cliente (McDonald's Style).
 * Fornece endpoints de consulta de opções de personalização e cálculo dinâmico de itens customizados.
 */
@RestController
@RequestMapping("/api/totem")
public class TotemController {

    private final OpcaoAdicionalService opcaoAdicionalService;
    private final PedidoService pedidoService;

    public TotemController(OpcaoAdicionalService opcaoAdicionalService, PedidoService pedidoService) {
        this.opcaoAdicionalService = opcaoAdicionalService;
        this.pedidoService = pedidoService;
    }

    /**
     * Retorna todas as opções e insumos adicionais disponíveis para um determinado produto.
     */
    @GetMapping("/produtos/{produtoId}/opcoes")
    public ResponseEntity<List<OpcaoAdicionalResponseDTO>> buscarOpcoesDoProduto(@PathVariable Long produtoId) {
        return ResponseEntity.ok(opcaoAdicionalService.buscarPorProduto(produtoId));
    }

    /**
     * Valida os adicionais, opções de combo e remoções de ingredientes, e calcula o subtotal exato
     * utilizando os valores cadastrados no banco de dados.
     */
    @PostMapping("/calcular-item")
    public ResponseEntity<ItemPedidoResponseDTO> calcularItem(@Valid @RequestBody CustomizacaoItemDTO dto) {
        ItemPedidoResponseDTO response = pedidoService.calcularItem(dto);
        return ResponseEntity.ok(response);
    }
}

