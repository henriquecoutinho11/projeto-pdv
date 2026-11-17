package com.restaurante.pdv.controller;

import com.restaurante.pdv.dto.request.ConfirmacaoPagamentoDTO;
import com.restaurante.pdv.dto.request.SimulacaoCartaoDTO;
import com.restaurante.pdv.dto.response.CobrancaPixResponseDTO;
import com.restaurante.pdv.dto.response.PagamentoResponseDTO;
import com.restaurante.pdv.model.service.PagamentoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para processamento de pagamentos do Totem e PDV (PIX, Cartão e Webhooks).
 */
@RestController
@RequestMapping("/api/pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    /**
     * Gera ou recupera a cobrança PIX instantânea para o pedido especificado.
     * Retorna o payload EMV copia-e-cola e a imagem do QR Code em Base64 com validade de 5 minutos.
     */
    @PostMapping("/{pedidoId}/pix")
    public ResponseEntity<CobrancaPixResponseDTO> gerarCobrancaPix(@PathVariable Long pedidoId) {
        CobrancaPixResponseDTO response = pagamentoService.gerarCobrancaPix(pedidoId);
        return ResponseEntity.ok(response);
    }

    /**
     * Consulta os dados e o temporizador restante da cobrança PIX do pedido.
     */
    @GetMapping("/{pedidoId}/pix")
    public ResponseEntity<CobrancaPixResponseDTO> consultarCobrancaPix(@PathVariable Long pedidoId) {
        CobrancaPixResponseDTO response = pagamentoService.consultarCobrancaPix(pedidoId);
        return ResponseEntity.ok(response);
    }

    /**
     * Simulação de webhook / endpoint de confirmação de pagamento aprovado.
     * Atualiza o pedido como pago e libera para preparo na cozinha.
     */
    @PostMapping("/{pedidoId}/confirmar")
    public ResponseEntity<PagamentoResponseDTO> confirmarPagamento(
            @PathVariable Long pedidoId,
            @Valid @RequestBody ConfirmacaoPagamentoDTO dto) {
        PagamentoResponseDTO response = pagamentoService.confirmarPagamento(pedidoId, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Simula a transação no terminal de cartão de crédito ou débito do totem de autoatendimento.
     */
    @PostMapping("/{pedidoId}/cartao")
    public ResponseEntity<PagamentoResponseDTO> simularPagamentoCartao(
            @PathVariable Long pedidoId,
            @Valid @RequestBody SimulacaoCartaoDTO dto) {
        PagamentoResponseDTO response = pagamentoService.simularPagamentoCartao(pedidoId, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Consulta o status de pagamento de determinado pedido (usado para polling pelo totem touch).
     */
    @GetMapping("/{pedidoId}/status")
    public ResponseEntity<PagamentoResponseDTO> consultarStatusPagamento(@PathVariable Long pedidoId) {
        PagamentoResponseDTO response = pagamentoService.consultarStatusPagamento(pedidoId);
        return ResponseEntity.ok(response);
    }
}
