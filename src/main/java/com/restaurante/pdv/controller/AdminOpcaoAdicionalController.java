package com.restaurante.pdv.controller;

import com.restaurante.pdv.dto.request.OpcaoAdicionalRequestDTO;
import com.restaurante.pdv.dto.response.OpcaoAdicionalResponseDTO;
import com.restaurante.pdv.model.entity.TipoOpcao;
import com.restaurante.pdv.model.service.OpcaoAdicionalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para administração de Opções Adicionais, Insumos e Opções de Combos.
 */
@RestController
@RequestMapping("/api/admin/opcoes")
public class AdminOpcaoAdicionalController {

    private final OpcaoAdicionalService opcaoAdicionalService;

    public AdminOpcaoAdicionalController(OpcaoAdicionalService opcaoAdicionalService) {
        this.opcaoAdicionalService = opcaoAdicionalService;
    }

    @GetMapping
    public ResponseEntity<List<OpcaoAdicionalResponseDTO>> listarTodas() {
        return ResponseEntity.ok(opcaoAdicionalService.listarTodas());
    }

    @GetMapping("/ativas")
    public ResponseEntity<List<OpcaoAdicionalResponseDTO>> buscarAtivas() {
        return ResponseEntity.ok(opcaoAdicionalService.buscarAtivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OpcaoAdicionalResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(opcaoAdicionalService.buscarPorId(id));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<OpcaoAdicionalResponseDTO>> buscarPorTipo(@PathVariable TipoOpcao tipo) {
        return ResponseEntity.ok(opcaoAdicionalService.buscarPorTipo(tipo));
    }

    @GetMapping("/produto/{produtoId}")
    public ResponseEntity<List<OpcaoAdicionalResponseDTO>> buscarPorProduto(@PathVariable Long produtoId) {
        return ResponseEntity.ok(opcaoAdicionalService.buscarPorProduto(produtoId));
    }

    @PostMapping
    public ResponseEntity<OpcaoAdicionalResponseDTO> salvar(@Valid @RequestBody OpcaoAdicionalRequestDTO dto) {
        OpcaoAdicionalResponseDTO salvo = opcaoAdicionalService.salvar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OpcaoAdicionalResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody OpcaoAdicionalRequestDTO dto) {
        OpcaoAdicionalResponseDTO atualizado = opcaoAdicionalService.atualizar(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        opcaoAdicionalService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        opcaoAdicionalService.reativar(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/produtos/{produtoId}/vincular/{opcaoId}")
    public ResponseEntity<Void> vincularAoProduto(@PathVariable Long produtoId, @PathVariable Long opcaoId) {
        opcaoAdicionalService.vincularAoProduto(produtoId, opcaoId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/produtos/{produtoId}/desvincular/{opcaoId}")
    public ResponseEntity<Void> desvincularDoProduto(@PathVariable Long produtoId, @PathVariable Long opcaoId) {
        opcaoAdicionalService.desvincularDoProduto(produtoId, opcaoId);
        return ResponseEntity.noContent().build();
    }
}

