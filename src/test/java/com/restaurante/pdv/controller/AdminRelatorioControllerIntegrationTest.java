package com.restaurante.pdv.controller;

import com.restaurante.pdv.model.entity.*;
import com.restaurante.pdv.model.repository.CategoriaRepository;
import com.restaurante.pdv.model.repository.PedidoRepository;
import com.restaurante.pdv.model.repository.ProdutoRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class AdminRelatorioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Produto produto1;
    private Produto produto2;

    @BeforeEach
    void setUp() {
        Categoria categoria = new Categoria("Lanches", "Lanches especiais");
        categoria = categoriaRepository.save(categoria);

        produto1 = new Produto(null, "X-Bacon Especial", "Delicioso", new BigDecimal("30.00"), 50, "/img1.jpg", true, categoria);
        produto1 = produtoRepository.save(produto1);

        produto2 = new Produto(null, "Batata Rústica", "Crocante", new BigDecimal("15.00"), 40, "/img2.jpg", true, categoria);
        produto2 = produtoRepository.save(produto2);

        // Criar Pedido Pago 1
        Pedido pedido1 = new Pedido();
        pedido1.setNumeroSenha(1);
        pedido1.setStatus(StatusPedido.PRONTO);
        pedido1.setFormaPagamento(FormaPagamento.PIX);
        pedido1.setPago(true);
        pedido1.setDataHora(LocalDateTime.now().minusMinutes(20));
        pedido1.setDataHoraPronto(LocalDateTime.now().minusMinutes(5));

        ItemPedido item1 = new ItemPedido(produto1, 2, new BigDecimal("30.00"), new BigDecimal("60.00"), null);
        pedido1.adicionarItem(item1);
        pedidoRepository.save(pedido1);

        // Criar Pedido Pago 2
        Pedido pedido2 = new Pedido();
        pedido2.setNumeroSenha(2);
        pedido2.setStatus(StatusPedido.ENTREGUE);
        pedido2.setFormaPagamento(FormaPagamento.CARTAO_CREDITO);
        pedido2.setPago(true);
        pedido2.setDataHora(LocalDateTime.now().minusMinutes(40));
        pedido2.setDataHoraPronto(LocalDateTime.now().minusMinutes(25));

        ItemPedido item2 = new ItemPedido(produto2, 1, new BigDecimal("15.00"), new BigDecimal("15.00"), null);
        pedido2.adicionarItem(item2);
        pedidoRepository.save(pedido2);
    }

    @Test
    @DisplayName("GET /api/admin/relatorios/faturamento - Deve retornar consolidado de faturamento")
    void deveRetornarRelatorioFaturamento() throws Exception {
        mockMvc.perform(get("/api/admin/relatorios/faturamento")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPedidos", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.totalFaturado", greaterThanOrEqualTo(75.00)))
                .andExpect(jsonPath("$.ticketMedio", notNullValue()))
                .andExpect(jsonPath("$.vendasPorFormaPagamento", notNullValue()))
                .andExpect(jsonPath("$.vendasPorStatus", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/admin/relatorios/fechamento-caixa - Deve retornar fechamento diário do caixa")
    void deveRetornarFechamentoCaixa() throws Exception {
        mockMvc.perform(get("/api/admin/relatorios/fechamento-caixa")
                        .param("data", LocalDate.now().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", is(LocalDate.now().toString())))
                .andExpect(jsonPath("$.totalPedidos", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.totalFaturado", greaterThanOrEqualTo(75.00)))
                .andExpect(jsonPath("$.totalPix", greaterThanOrEqualTo(60.00)))
                .andExpect(jsonPath("$.totalCartaoCredito", greaterThanOrEqualTo(15.00)))
                .andExpect(jsonPath("$.detalhesFormasPagamento", hasSize(greaterThanOrEqualTo(4))));
    }

    @Test
    @DisplayName("GET /api/admin/relatorios/produtos-mais-vendidos - Deve retornar curva ABC dos produtos")
    void deveRetornarProdutosMaisVendidos() throws Exception {
        mockMvc.perform(get("/api/admin/relatorios/produtos-mais-vendidos")
                        .param("limite", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].nomeProduto", notNullValue()))
                .andExpect(jsonPath("$[0].classificacaoAbc", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/admin/relatorios/tempo-preparo - Deve retornar métricas de tempo de preparo da cozinha")
    void deveRetornarTempoMedioPreparo() throws Exception {
        mockMvc.perform(get("/api/admin/relatorios/tempo-preparo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPedidosAvaliados", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.tempoMedioMinutos", greaterThan(0.0)));
    }

    @Test
    @DisplayName("GET /api/admin/relatorios/resumo - Deve retornar visão executiva resumida")
    void deveRetornarResumoGeral() throws Exception {
        mockMvc.perform(get("/api/admin/relatorios/resumo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFaturado", notNullValue()))
                .andExpect(jsonPath("$.totalPedidos", notNullValue()))
                .andExpect(jsonPath("$.ticketMedio", notNullValue()))
                .andExpect(jsonPath("$.topProdutosMaisVendidos", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/admin/relatorios/faturamento - Deve retornar erro 400 quando datas forem inconsistentes")
    void deveRetornarErroQuandoDataInicioPosteriorAFim() throws Exception {
        String inicio = LocalDateTime.now().plusDays(2).toString();
        String fim = LocalDateTime.now().toString();

        mockMvc.perform(get("/api/admin/relatorios/faturamento")
                        .param("inicio", inicio)
                        .param("fim", fim)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("A data inicial não pode ser posterior")));
    }
}
