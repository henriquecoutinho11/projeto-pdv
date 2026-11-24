package com.restaurante.pdv.controller;

import com.restaurante.pdv.dto.CategoriaDTO;
import com.restaurante.pdv.dto.ProdutoResponseDTO;
import com.restaurante.pdv.dto.request.ModalidadeConsumoDTO;
import com.restaurante.pdv.model.entity.TipoConsumo;
import com.restaurante.pdv.model.service.CategoriaService;
import com.restaurante.pdv.model.service.ProdutoService;
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
import java.util.List;
import java.util.Map;

/**
 * Controller MVC para o Totem de Autoatendimento do Antunes Lanches.
 * Gerencia a renderização dos templates Thymeleaf da jornada do cliente,
 * além do controle de sessão da modalidade de consumo (Comer Aqui / Para Viagem)
 * e o carregamento da vitrine de produtos e categorias do cardápio digital.
 */
@Controller
public class TotemViewController {

    public static final String SESSION_TIPO_CONSUMO = "TIPO_CONSUMO";
    public static final String SESSION_NOME_CLIENTE = "NOME_CLIENTE";
    public static final String SESSION_CPF_CLIENTE = "CPF_CLIENTE";

    private final CategoriaService categoriaService;
    private final ProdutoService produtoService;

    public TotemViewController(CategoriaService categoriaService, ProdutoService produtoService) {
        this.categoriaService = categoriaService;
        this.produtoService = produtoService;
    }

    /**
     * Redireciona a raiz da aplicação para o Totem de Autoatendimento.
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/totem";
    }

    /**
     * Renderiza a página principal do Totem (Welcome, Seleção de Modalidade e Vitrine do Cardápio).
     */
    @GetMapping("/totem")
    public String totem(Model model, HttpSession session) {
        carregarDadosCardapio(model, session);
        return "totem/index";
    }

    /**
     * Acesso direto à vitrine do Cardápio do Totem.
     */
    @GetMapping("/totem/cardapio")
    public String cardapio(Model model, HttpSession session) {
        carregarDadosCardapio(model, session);
        model.addAttribute("abrirCardapioDireto", true);
        return "totem/index";
    }

    private void carregarDadosCardapio(Model model, HttpSession session) {
        TipoConsumo tipoConsumo = (TipoConsumo) session.getAttribute(SESSION_TIPO_CONSUMO);
        List<CategoriaDTO> categorias = categoriaService.buscarAtivas();
        List<ProdutoResponseDTO> produtos = produtoService.buscarAtivos();

        model.addAttribute("tipoConsumo", tipoConsumo);
        model.addAttribute("categorias", categorias);
        model.addAttribute("produtos", produtos);
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
     * Registra os dados de identificação do cliente (Nome e CPF) na sessão.
     */
    @PostMapping("/totem/identificacao")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registrarIdentificacao(
            @RequestBody Map<String, String> payload,
            HttpSession session) {
        String nome = payload != null ? payload.get("nomeCliente") : null;
        String cpf = payload != null ? payload.get("cpfCliente") : null;

        session.setAttribute(SESSION_NOME_CLIENTE, nome);
        session.setAttribute(SESSION_CPF_CLIENTE, cpf);

        Map<String, Object> response = new HashMap<>();
        response.put("sucesso", true);
        response.put("nomeCliente", nome);
        response.put("cpfCliente", cpf);

        return ResponseEntity.ok(response);
    }

    /**
     * Reseta e limpa o estado de atendimento da sessão do cliente.
     */
    @PostMapping("/totem/reset")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetarSessao(HttpSession session) {
        session.removeAttribute(SESSION_TIPO_CONSUMO);
        session.removeAttribute(SESSION_NOME_CLIENTE);
        session.removeAttribute(SESSION_CPF_CLIENTE);

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
        String nomeCliente = (String) session.getAttribute(SESSION_NOME_CLIENTE);
        String cpfCliente = (String) session.getAttribute(SESSION_CPF_CLIENTE);

        Map<String, Object> response = new HashMap<>();
        response.put("tipoConsumo", tipoConsumo);
        response.put("nomeCliente", nomeCliente);
        response.put("cpfCliente", cpfCliente);
        response.put("ativo", tipoConsumo != null);

        return ResponseEntity.ok(response);
    }
}
