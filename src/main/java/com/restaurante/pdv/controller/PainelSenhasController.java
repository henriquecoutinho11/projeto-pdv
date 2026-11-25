package com.restaurante.pdv.controller;

import com.restaurante.pdv.dto.response.PedidoResponseDTO;
import com.restaurante.pdv.model.service.PedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * Controller MVC e API de consulta para o Painel Público de Chamada de Senhas (TV do Salão).
 */
@Controller
public class PainelSenhasController {

    private final PedidoService pedidoService;

    public PainelSenhasController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /**
     * Renderiza a página do Painel de TV do Salão.
     */
    @GetMapping("/painel-senhas")
    public String painelSenhas() {
        return "senhas/index";
    }

    /**
     * Retorna as senhas em preparo (RECEBIDO / EM_PREPARO) e prontas para retirada (PRONTO)
     * para atualização em tempo real do painel de TV sem recarregamento de página.
     */
    @GetMapping("/api/painel-senhas")
    @ResponseBody
    public ResponseEntity<Map<String, List<PedidoResponseDTO>>> listarSenhas() {
        return ResponseEntity.ok(pedidoService.listarPainelSenhas());
    }
}
