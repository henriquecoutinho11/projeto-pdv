package com.restaurante.pdv.controller;

import com.restaurante.pdv.dto.request.ModalidadeConsumoDTO;
import com.restaurante.pdv.model.entity.TipoConsumo;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller MVC para o Totem de Autoatendimento do Antunes Lanches.
 * Gerencia a renderização dos templates Thymeleaf da jornada do cliente,
 * além do controle de sessão da modalidade de consumo (Comer Aqui / Para Viagem).
 */
@Controller
public class TotemViewController {

    public static final String SESSION_TIPO_CONSUMO = "TIPO_CONSUMO";

    /**
     * Redireciona a raiz da aplicação para o Totem de Autoatendimento.
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/totem";
    }

    /**
     * Renderiza a página principal do Totem (Welcome & Seleção de Modalidade).
     */
    @GetMapping("/totem")
    public String totem(Model model, HttpSession session) {
        TipoConsumo tipoConsumo = (TipoConsumo) session.getAttribute(SESSION_TIPO_CONSUMO);
        model.addAttribute("tipoConsumo", tipoConsumo);
        return "totem/index";
    }

    /**
     * Registra a modalidade de consumo selecionada na sessão do cliente.
     */
    @PostMapping("/totem/modalidade")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registrarModalidade(
            @Valid @RequestBody ModalidadeConsumoDTO dto,
            HttpSession session) {
        session.setAttribute(SESSION_TIPO_CONSUMO, dto.tipoConsumo());

        Map<String, Object> response = new HashMap<>();
        response.put("sucesso", true);
        response.put("tipoConsumo", dto.tipoConsumo());
        response.put("descricao", dto.tipoConsumo().getDescricao());

        return ResponseEntity.ok(response);
    }

    /**
     * Reseta e limpa o estado de atendimento da sessão do cliente.
     */
    @PostMapping("/totem/reset")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetarSessao(HttpSession session) {
        session.removeAttribute(SESSION_TIPO_CONSUMO);

        Map<String, Object> response = new HashMap<>();
        response.put("sucesso", true);
        response.put("mensagem", "Sessão do totem resetada com sucesso.");

        return ResponseEntity.ok(response);
    }

    /**
     * Consulta o estado atual da sessão do Totem.
     */
    @GetMapping("/totem/sessao")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> consultarSessao(HttpSession session) {
        TipoConsumo tipoConsumo = (TipoConsumo) session.getAttribute(SESSION_TIPO_CONSUMO);

        Map<String, Object> response = new HashMap<>();
        response.put("tipoConsumo", tipoConsumo);
        response.put("ativo", tipoConsumo != null);

        return ResponseEntity.ok(response);
    }
}
