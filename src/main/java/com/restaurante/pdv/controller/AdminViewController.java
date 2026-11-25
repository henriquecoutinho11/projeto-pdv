package com.restaurante.pdv.controller;

import com.restaurante.pdv.dto.CategoriaDTO;
import com.restaurante.pdv.dto.ProdutoResponseDTO;
import com.restaurante.pdv.dto.response.ResumoVendasDTO;
import com.restaurante.pdv.model.service.CategoriaService;
import com.restaurante.pdv.model.service.ProdutoService;
import com.restaurante.pdv.model.service.RelatorioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller MVC para o Painel Administrativo de Gestão do Antunes Lanches.
 * Fornece a visão de retaguarda para gestão de cardápio, controle de estoque
 * e dashboard analítico de métricas e faturamento.
 */
@Controller
public class AdminViewController {

    private final CategoriaService categoriaService;
    private final ProdutoService produtoService;
    private final RelatorioService relatorioService;

    public AdminViewController(CategoriaService categoriaService,
                               ProdutoService produtoService,
                               RelatorioService relatorioService) {
        this.categoriaService = categoriaService;
        this.produtoService = produtoService;
        this.relatorioService = relatorioService;
    }

    /**
     * Renderiza o painel administrativo integrado (Dashboard + Gestão de Produtos e Estoque).
     */
    @GetMapping({"/admin", "/admin/", "/admin/dashboard", "/admin/produtos", "/admin/relatorios"})
    public String admin(Model model) {
        List<CategoriaDTO> categorias = categoriaService.listarTodas();
        List<ProdutoResponseDTO> produtos = produtoService.listarTodos();
        ResumoVendasDTO resumo = relatorioService.gerarResumoGeral(null, null);

        model.addAttribute("categorias", categorias);
        model.addAttribute("produtos", produtos);
        model.addAttribute("resumo", resumo);

        return "admin/index";
    }
}
