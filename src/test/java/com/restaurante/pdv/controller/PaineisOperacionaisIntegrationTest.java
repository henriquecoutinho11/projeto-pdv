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
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@DisplayName("Testes de Integração dos Painéis Operacionais (KDS & Painel de Senhas)")
class PaineisOperacionaisIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    private Produto produto;

    @BeforeEach
    void setUp() {
        Categoria cat = categoriaRepository.save(new Categoria("Lanches Painel", "Categoria de teste"));
        produto = produtoRepository.save(new Produto(
                null, "X-Painel", "Lanche de teste",
                new BigDecimal("25.00"), 20, "/img/burger-hero.svg", true, cat
        ));
    }

    private Pedido criarPedido(StatusPedido status, Integer senha, String nomeCliente, LocalDateTime dataHora) {
        Pedido pedido = new Pedido();
        pedido.setStatus(status);
        pedido.setPago(true);
        pedido.setNumeroSenha(senha);
        pedido.setNomeCliente(nomeCliente);
        pedido.setFormaPagamento(FormaPagamento.PIX);
        pedido.setDataHora(dataHora);

        ItemPedido item = new ItemPedido(produto, 1, new BigDecimal("25.00"), new BigDecimal("25.00"), null);
        pedido.adicionarItem(item);
        return pedidoRepository.save(pedido);
    }

    @Test
    @DisplayName("GET /kds deve renderizar a view kds/index com assets do KDS")
    void deveRenderizarViewKds() throws Exception {
        mockMvc.perform(get("/kds"))
                .andExpect(status().isOk())
                .andExpect(view().name("kds/index"))
                .andExpect(content().string(containsString("ANTUNES")))
                .andExpect(content().string(containsString("kds.css")))
                .andExpect(content().string(containsString("kds.js")));
    }

    @Test
    @DisplayName("GET /painel-senhas deve renderizar a view senhas/index com colunas EM PREPARO e PRONTO PARA RETIRADA")
    void deveRenderizarViewPainelSenhas() throws Exception {
        mockMvc.perform(get("/painel-senhas"))
                .andExpect(status().isOk())
                .andExpect(view().name("senhas/index"))
                .andExpect(content().string(containsString("EM PREPARO")))
                .andExpect(content().string(containsString("PRONTO PARA RETIRADA")))
                .andExpect(content().string(containsString("painel-senhas.css")))
                .andExpect(content().string(containsString("painel-senhas.js")));
    }

    @Test
    @DisplayName("GET /api/kds/pedidos-prontos deve retornar apenas pedidos com status PRONTO")
    void deveListarPedidosProntosNoKds() throws Exception {
        LocalDateTime agora = LocalDateTime.now();
        criarPedido(StatusPedido.RECEBIDO, 10, "Ana", agora.minusMinutes(3));
        criarPedido(StatusPedido.EM_PREPARO, 11, "Bruno", agora.minusMinutes(6));
        Pedido pronto = criarPedido(StatusPedido.PRONTO, 12, "Carlos", agora.minusMinutes(9));
        criarPedido(StatusPedido.ENTREGUE, 13, "Daniela", agora.minusMinutes(15));

        mockMvc.perform(get("/api/kds/pedidos-prontos").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(pronto.getId()))
                .andExpect(jsonPath("$[0].status").value("PRONTO"))
                .andExpect(jsonPath("$[0].senhaFormatada").value("#012"));
    }

    @Test
    @DisplayName("GET /api/painel-senhas deve separar senhas em preparo e prontas para retirada")
    void deveRetornarSenhasAgrupadasParaPainelTv() throws Exception {
        LocalDateTime agora = LocalDateTime.now();
        criarPedido(StatusPedido.RECEBIDO, 21, "Maria", agora.minusMinutes(2));
        criarPedido(StatusPedido.EM_PREPARO, 22, "João", agora.minusMinutes(5));
        criarPedido(StatusPedido.PRONTO, 23, "Fernanda", agora.minusMinutes(8));
        criarPedido(StatusPedido.ENTREGUE, 24, "Lucas", agora.minusMinutes(12));

        mockMvc.perform(get("/api/painel-senhas").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emPreparo", hasSize(2)))
                .andExpect(jsonPath("$.emPreparo[*].numeroSenha", contains(22, 21)))
                .andExpect(jsonPath("$.prontos", hasSize(1)))
                .andExpect(jsonPath("$.prontos[0].numeroSenha").value(23))
                .andExpect(jsonPath("$.prontos[0].nomeCliente").value("Fernanda"));
    }
}
