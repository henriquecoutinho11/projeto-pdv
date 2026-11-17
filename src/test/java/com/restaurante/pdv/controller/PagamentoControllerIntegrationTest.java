package com.restaurante.pdv.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.pdv.dto.request.ConfirmacaoPagamentoDTO;
import com.restaurante.pdv.dto.request.CustomizacaoItemDTO;
import com.restaurante.pdv.dto.request.NovoPedidoDTO;
import com.restaurante.pdv.dto.request.SimulacaoCartaoDTO;
import com.restaurante.pdv.model.entity.*;
import com.restaurante.pdv.model.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@DisplayName("Testes de Integração - PagamentoController e Fluxo de Pagamento")
class PagamentoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private CobrancaPixRepository cobrancaPixRepository;

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        cobrancaPixRepository.deleteAll();
        pedidoRepository.deleteAll();
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();

        Categoria categoria = new Categoria();
        categoria.setNome("Lanches Especiais");
        categoria.setAtivo(true);
        categoria = categoriaRepository.save(categoria);

        Produto burguer = new Produto();
        burguer.setNome("Antunes Smash");
        burguer.setDescricao("Hambúrguer artesanal");
        burguer.setPreco(new BigDecimal("30.00"));
        burguer.setQuantidadeEstoque(50);
        burguer.setAtivo(true);
        burguer.setCategoria(categoria);
        burguer = produtoRepository.save(burguer);

        pedido = new Pedido();
        pedido.setStatus(StatusPedido.RECEBIDO);
        pedido.setFormaPagamento(FormaPagamento.PIX);
        pedido.setNomeCliente("Isabelly");
        pedido.setCpfCliente("123.456.789-00");
        pedido.setPago(Boolean.FALSE);

        ItemPedido item = new ItemPedido();
        item.setProduto(burguer);
        item.setQuantidade(1);
        item.setPrecoUnitario(new BigDecimal("30.00"));
        item.setSubtotal(new BigDecimal("30.00"));
        pedido.adicionarItem(item);

        pedido = pedidoRepository.save(pedido);
    }

    @Test
    @DisplayName("POST /api/pagamentos/{pedidoId}/pix - Deve gerar cobrança PIX com payload e imagem QR Code legível")
    void deveGerarCobrancaPixEndpoint() throws Exception {
        mockMvc.perform(post("/api/pagamentos/{pedidoId}/pix", pedido.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoId", is(pedido.getId().intValue())))
                .andExpect(jsonPath("$.valor", is(30.00)))
                .andExpect(jsonPath("$.payloadEmv", startsWith("000201")))
                .andExpect(jsonPath("$.payloadEmv", containsString("br.gov.bcb.pix")))
                .andExpect(jsonPath("$.qrCodeBase64", notNullValue()))
                .andExpect(jsonPath("$.qrCodeDataUri", startsWith("data:image/png;base64,")))
                .andExpect(jsonPath("$.segundosRestantes", greaterThan(250)))
                .andExpect(jsonPath("$.expirado", is(false)))
                .andExpect(jsonPath("$.pago", is(false)));

        // Consulta subsequente deve recuperar a cobrança ativa
        mockMvc.perform(get("/api/pagamentos/{pedidoId}/pix", pedido.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoId", is(pedido.getId().intValue())))
                .andExpect(jsonPath("$.expirado", is(false)));
    }

    @Test
    @DisplayName("POST /api/pagamentos/{pedidoId}/confirmar - Deve confirmar pagamento PIX com sucesso")
    void deveConfirmarPagamentoPixEndpoint() throws Exception {
        // 1. Gera a cobrança PIX
        mockMvc.perform(post("/api/pagamentos/{pedidoId}/pix", pedido.getId()))
                .andExpect(status().isOk());

        // 2. Confirma o pagamento via webhook / endpoint
        ConfirmacaoPagamentoDTO confirmacao = new ConfirmacaoPagamentoDTO(FormaPagamento.PIX, new BigDecimal("30.00"), "TX-BR-1234");

        mockMvc.perform(post("/api/pagamentos/{pedidoId}/confirmar", pedido.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmacao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoId", is(pedido.getId().intValue())))
                .andExpect(jsonPath("$.pago", is(true)))
                .andExpect(jsonPath("$.formaPagamento", is("PIX")))
                .andExpect(jsonPath("$.valorPago", is(30.00)))
                .andExpect(jsonPath("$.codigoTransacao", is("TX-BR-1234")))
                .andExpect(jsonPath("$.mensagem", containsString("sucesso")));

        // 3. Consulta status atualizado
        mockMvc.perform(get("/api/pagamentos/{pedidoId}/status", pedido.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pago", is(true)))
                .andExpect(jsonPath("$.mensagem", containsString("sucesso")));
    }

    @Test
    @DisplayName("POST /api/pagamentos/{pedidoId}/confirmar - Deve rejeitar valor divergente com 400 Bad Request")
    void deveRejeitarValorDivergenteEndpoint() throws Exception {
        mockMvc.perform(post("/api/pagamentos/{pedidoId}/pix", pedido.getId()))
                .andExpect(status().isOk());

        ConfirmacaoPagamentoDTO confirmacao = new ConfirmacaoPagamentoDTO(FormaPagamento.PIX, new BigDecimal("15.00"));

        mockMvc.perform(post("/api/pagamentos/{pedidoId}/confirmar", pedido.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmacao)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Regra de Negócio Violada")))
                .andExpect(jsonPath("$.detail", containsString("difere do valor total do pedido")));
    }

    @Test
    @DisplayName("POST /api/pagamentos/{pedidoId}/confirmar - Deve rejeitar forma de pagamento inválida com 400 Bad Request")
    void deveRejeitarFormaPagamentoInvalida() throws Exception {
        String payloadJsonInvalido = """
                {
                    "formaPagamento": "CRIPTOMOEDA_BITCOIN",
                    "valorPago": 30.00
                }
                """;

        mockMvc.perform(post("/api/pagamentos/{pedidoId}/confirmar", pedido.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadJsonInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/pagamentos/{pedidoId}/cartao - Deve simular transação de cartão de crédito no totem")
    void deveSimularPagamentoCartaoEndpoint() throws Exception {
        SimulacaoCartaoDTO simulacao = new SimulacaoCartaoDTO(FormaPagamento.CARTAO_CREDITO, "MASTERCARD");
        simulacao.setTipoOperacao("APROXIMACAO");

        mockMvc.perform(post("/api/pagamentos/{pedidoId}/cartao", pedido.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(simulacao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pago", is(true)))
                .andExpect(jsonPath("$.formaPagamento", is("CARTAO_CREDITO")))
                .andExpect(jsonPath("$.valorPago", is(30.00)))
                .andExpect(jsonPath("$.codigoTransacao", startsWith("NSU-")));
    }

    @Test
    @DisplayName("Critério de Aceite: Pedido só pode ter status alterado para preparo na cozinha após a confirmação do pagamento")
    void deveBloquearPreparoNaCozinhaAntesDoPagamento() throws Exception {
        // Pedido recém-criado, NÃO pago -> tentativa de avançar para EM_PREPARO deve ser rejeitada
        mockMvc.perform(patch("/api/pedidos/{id}/status", pedido.getId())
                        .param("status", "EM_PREPARO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Regra de Negócio Violada")))
                .andExpect(jsonPath("$.detail", containsString("não pode ter status alterado para preparo na cozinha antes da confirmação do pagamento")));

        // Efetua o pagamento do pedido via Cartão de Débito
        SimulacaoCartaoDTO simulacao = new SimulacaoCartaoDTO(FormaPagamento.CARTAO_DEBITO, "VISA");
        mockMvc.perform(post("/api/pagamentos/{pedidoId}/cartao", pedido.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(simulacao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pago", is(true)));

        // Agora que o pedido está pago, o avanço para EM_PREPARO deve ocorrer com sucesso
        mockMvc.perform(patch("/api/pedidos/{id}/status", pedido.getId())
                        .param("status", "EM_PREPARO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("EM_PREPARO")));
    }
}
