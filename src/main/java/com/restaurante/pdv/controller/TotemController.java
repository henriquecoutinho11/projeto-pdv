package com.restaurante.pdv.controller;

import com.restaurante.pdv.dto.CategoriaDTO;
import com.restaurante.pdv.dto.ProdutoResponseDTO;
import com.restaurante.pdv.dto.request.CustomizacaoItemDTO;
import com.restaurante.pdv.dto.response.ItemPedidoResponseDTO;
import com.restaurante.pdv.dto.response.OpcaoAdicionalResponseDTO;
import com.restaurante.pdv.model.service.CategoriaService;
import com.restaurante.pdv.model.service.OpcaoAdicionalService;
import com.restaurante.pdv.model.service.PedidoService;
import com.restaurante.pdv.model.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller REST para o Totem de Autoatendimento do Cliente (McDonald's Style).
 * Fornece endpoints de consulta do cardápio, opções de personalização e cálculo dinâmico de itens customizados.
 */
@RestController
@RequestMapping("/api/totem")
public class TotemController {

    private final OpcaoAdicionalService opcaoAdicionalService;
    private final PedidoService pedidoService;
    private final CategoriaService categoriaService;
    private final ProdutoService produtoService;

    public TotemController(OpcaoAdicionalService opcaoAdicionalService,
                           PedidoService pedidoService,
                           CategoriaService categoriaService,
                           ProdutoService produtoService) {
        this.opcaoAdicionalService = opcaoAdicionalService;
        this.pedidoService = pedidoService;
        this.categoriaService = categoriaService;
        this.produtoService = produtoService;
    }

    /**
     * Retorna o catálogo completo do cardápio ativo para o Totem (categorias e produtos).
     */
    @GetMapping("/cardapio")
    public ResponseEntity<Map<String, Object>> buscarCardapioCompleto() {
        Map<String, Object> cardapio = new HashMap<>();
        cardapio.put("categorias", categoriaService.buscarAtivas());
        cardapio.put("produtos", produtoService.buscarAtivos());
        return ResponseEntity.ok(cardapio);
    }

    /**
     * Retorna todos os produtos ativos cadastrados no cardápio do Totem.
     */
    @GetMapping("/produtos")
    public ResponseEntity<List<ProdutoResponseDTO>> listarProdutosTotem() {
        return ResponseEntity.ok(produtoService.buscarAtivos());
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
