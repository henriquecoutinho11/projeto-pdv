package com.restaurante.pdv.controller;

import com.restaurante.pdv.dto.response.*;
import com.restaurante.pdv.model.service.RelatorioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller REST com endpoints administrativos para relatórios gerenciais, curva ABC e fechamento de vendas.
 */
@RestController
@RequestMapping("/api/admin/relatorios")
public class AdminRelatorioController {

    private final RelatorioService relatorioService;

    public AdminRelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    /**
     * Retorna o relatório analítico de faturamento e vendas por período.
     */
    @GetMapping("/faturamento")
    public ResponseEntity<RelatorioFaturamentoDTO> obterRelatorioFaturamento(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(relatorioService.gerarRelatorioFaturamento(inicio, fim));
    }

    /**
     * Retorna o fechamento de caixa diário detalhado por forma de pagamento.
     * Caso o parâmetro data não seja informado, consolida o dia atual.
     */
    @GetMapping("/fechamento-caixa")
    public ResponseEntity<FechamentoCaixaDTO> obterFechamentoCaixa(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(relatorioService.gerarFechamentoCaixa(data));
    }

    /**
     * Retorna o ranking de produtos mais vendidos com Curva ABC (Pareto).
     */
    @GetMapping("/produtos-mais-vendidos")
    public ResponseEntity<List<ItemMaisVendidoDTO>> obterProdutosMaisVendidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            @RequestParam(required = false, defaultValue = "10") Integer limite) {
        return ResponseEntity.ok(relatorioService.obterRankingProdutosMaisVendidos(inicio, fim, limite));
    }

    /**
     * Retorna métricas de tempo de preparo dos pedidos concluídos na cozinha (KDS).
     */
    @GetMapping("/tempo-preparo")
    public ResponseEntity<TempoMedioPreparoDTO> obterTempoMedioPreparo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(relatorioService.calcularTempoMedioPreparo(inicio, fim));
    }

    /**
     * Retorna uma visão executiva consolidada (Resumo Geral de Vendas).
     */
    @GetMapping("/resumo")
    public ResponseEntity<ResumoVendasDTO> obterResumoGeral(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(relatorioService.gerarResumoGeral(inicio, fim));
    }
}
