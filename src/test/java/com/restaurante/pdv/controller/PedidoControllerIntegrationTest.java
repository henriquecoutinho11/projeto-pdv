package com.restaurante.pdv.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.pdv.dto.request.CustomizacaoItemDTO;
import com.restaurante.pdv.dto.request.NovoPedidoDTO;
import com.restaurante.pdv.model.entity.*;
import com.restaurante.pdv.model.repository.CategoriaRepository;
import com.restaurante.pdv.model.repository.OpcaoAdicionalRepository;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class PedidoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private OpcaoAdicionalRepository opcaoAdicionalRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    private Produto burguer;
    private Produto combo;
    private OpcaoAdicional bacon;
    private OpcaoAdicional batataGrande;

    @BeforeEach
    void setUp() {
        Categoria cat = categoriaRepository.save(new Categoria("Lanches Teste", "Categoria teste"));

        bacon = opcaoAdicionalRepository.save(new OpcaoAdicional(
                "Bacon Extra", "Fatias de bacon crocante",
                new BigDecimal("4.00"), TipoOpcao.ADICIONAL, false, true
        ));

        batataGrande = opcaoAdicionalRepository.save(new OpcaoAdicional(
                "Batata Grande", "Batata tamanho grande",
                new BigDecimal("5.00"), TipoOpcao.TAMANHO_BATATA, false, true
        ));

        burguer = new Produto(
                null, "X-Bacon Especial", "Hambúrguer com bacon",
                new BigDecimal("25.00"), 10, "http://img.com/bacon.png", true, cat
        );
        burguer.setOpcoesDisponiveis(new HashSet<>(Set.of(bacon)));
        burguer = produtoRepository.save(burguer);

        combo = new Produto(
                null, "Combo Master", "Combo completo",
                new BigDecimal("40.00"), 5, "http://img.com/combo.png", true, cat
        );
        combo.setOpcoesDisponiveis(new HashSet<>(Set.of(batataGrande)));
        combo = produtoRepository.save(combo);
    }

    @Test
    @DisplayName("POST /api/pedidos - Deve criar novo pedido com sucesso, retornar 201 Created e debitar estoque")
    void deveCriarPedidoComSucesso() throws Exception {
        CustomizacaoItemDTO item1 = new CustomizacaoItemDTO(burguer.getId(), 2);
        item1.setAdicionaisIds(List.of(bacon.getId())); // 25.00 + 4.00 = 29.00 * 2 = 58.00

        CustomizacaoItemDTO item2 = new CustomizacaoItemDTO(combo.getId(), 1);
        item2.setTamanhoBatataId(batataGrande.getId()); // 40.00 + 5.00 = 45.00 * 1 = 45.00

        NovoPedidoDTO dto = new NovoPedidoDTO(List.of(item1, item2), FormaPagamento.PIX, "Carlos Antunes", "123.456.789-00");

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("RECEBIDO"))
                .andExpect(jsonPath("$.formaPagamento").value("PIX"))
                .andExpect(jsonPath("$.nomeCliente").value("Carlos Antunes"))
                .andExpect(jsonPath("$.cpfCliente").value("123.456.789-00"))
                .andExpect(jsonPath("$.valorTotal").value(103.00)) // 58.00 + 45.00 = 103.00
                .andExpect(jsonPath("$.quantidadeItens").value(3))
                .andExpect(jsonPath("$.itens", hasSize(2)))
                .andExpect(jsonPath("$.itens[0].subtotal").value(58.00))
                .andExpect(jsonPath("$.itens[1].subtotal").value(45.00));

        // Verificar estoque debitado
        Produto burguerAtualizado = produtoRepository.findById(burguer.getId()).orElseThrow();
        assertEquals(8, burguerAtualizado.getQuantidadeEstoque()); // 10 - 2 = 8

        Produto comboAtualizado = produtoRepository.findById(combo.getId()).orElseThrow();
        assertEquals(4, comboAtualizado.getQuantidadeEstoque()); // 5 - 1 = 4
    }

    @Test
    @DisplayName("POST /api/pedidos - Deve retornar 400 Bad Request e manter estoque inalterado quando estoque for insuficiente")
    void deveRetornar400QuandoEstoqueInsuficiente() throws Exception {
        // combo possui estoque 5, solicitamos 6
        CustomizacaoItemDTO item = new CustomizacaoItemDTO(combo.getId(), 6);
        NovoPedidoDTO dto = new NovoPedidoDTO(List.of(item), FormaPagamento.CARTAO_CREDITO, null, null);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Regra de Negócio Violada"))
                .andExpect(jsonPath("$.detail", containsString("Estoque insuficiente")));

        // Verificar que o estoque permaneceu intacto (rollback)
        Produto comboDb = produtoRepository.findById(combo.getId()).orElseThrow();
        assertEquals(5, comboDb.getQuantidadeEstoque());
    }

    @Test
    @DisplayName("POST /api/pedidos - Deve retornar 400 Bad Request quando lista de itens for vazia")
    void deveRetornar400QuandoListaItensVazia() throws Exception {
        NovoPedidoDTO dto = new NovoPedidoDTO(Collections.emptyList());

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados Inválidos"));
    }

    @Test
    @DisplayName("POST /api/pedidos - Deve retornar 400 Bad Request quando produto estiver inativo")
    void deveRetornar400QuandoProdutoInativo() throws Exception {
        burguer.setAtivo(false);
        produtoRepository.save(burguer);

        CustomizacaoItemDTO item = new CustomizacaoItemDTO(burguer.getId(), 1);
        NovoPedidoDTO dto = new NovoPedidoDTO(List.of(item));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Regra de Negócio Violada"))
                .andExpect(jsonPath("$.detail", containsString("está inativo")));
    }

    @Test
    @DisplayName("GET /api/pedidos/{id} - Deve retornar pedido detalhado por ID")
    void deveBuscarPedidoPorId() throws Exception {
        CustomizacaoItemDTO item = new CustomizacaoItemDTO(burguer.getId(), 1);
        NovoPedidoDTO dto = new NovoPedidoDTO(List.of(item), FormaPagamento.PIX, "Cliente Teste", null);

        String jsonResponse = mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long idCriado = objectMapper.readTree(jsonResponse).get("id").asLong();

        mockMvc.perform(get("/api/pedidos/{id}", idCriado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idCriado))
                .andExpect(jsonPath("$.status").value("RECEBIDO"))
                .andExpect(jsonPath("$.nomeCliente").value("Cliente Teste"))
                .andExpect(jsonPath("$.valorTotal").value(25.00));
    }

    @Test
    @DisplayName("GET /api/pedidos/{id} - Deve retornar 404 quando pedido não existir")
    void deveRetornar404QuandoPedidoNaoExistir() throws Exception {
        mockMvc.perform(get("/api/pedidos/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso Não Encontrado"));
    }

    @Test
    @DisplayName("GET /api/pedidos - Deve listar todos os pedidos")
    void deveListarTodosOsPedidos() throws Exception {
        CustomizacaoItemDTO item = new CustomizacaoItemDTO(burguer.getId(), 1);
        NovoPedidoDTO dto = new NovoPedidoDTO(List.of(item), FormaPagamento.DINHEIRO, "Cliente Lista", null);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    @DisplayName("GET /api/pedidos/status/{status} - Deve filtrar pedidos por status")
    void deveFiltrarPedidosPorStatus() throws Exception {
        CustomizacaoItemDTO item = new CustomizacaoItemDTO(burguer.getId(), 1);
        NovoPedidoDTO dto = new NovoPedidoDTO(List.of(item), FormaPagamento.PIX, "Cliente Status", null);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/pedidos/status/RECEBIDO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].status").value("RECEBIDO"));
    }
}
