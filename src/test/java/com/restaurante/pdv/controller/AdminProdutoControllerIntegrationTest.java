package com.restaurante.pdv.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.pdv.dto.EstoqueUpdateDTO;
import com.restaurante.pdv.dto.ProdutoRequestDTO;
import com.restaurante.pdv.model.entity.Categoria;
import com.restaurante.pdv.model.entity.Produto;
import com.restaurante.pdv.model.repository.CategoriaRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class AdminProdutoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    private Categoria categoriaHamburguer;

    @BeforeEach
    void setUp() {
        categoriaHamburguer = new Categoria("Hambúrgueres Teste", "Categoria para testes");
        categoriaHamburguer = categoriaRepository.save(categoriaHamburguer);
    }

    @Test
    @DisplayName("POST /api/admin/produtos - Deve criar produto com sucesso")
    void deveCriarProdutoComSucesso() throws Exception {
        ProdutoRequestDTO request = new ProdutoRequestDTO(
                "X-Salada Especial",
                "Lanche com queijo e salada",
                new BigDecimal("26.50"),
                30,
                "/img/xsalada.jpg",
                true,
                categoriaHamburguer.getId()
        );

        mockMvc.perform(post("/api/admin/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nome", is("X-Salada Especial")))
                .andExpect(jsonPath("$.preco", is(26.50)))
                .andExpect(jsonPath("$.quantidadeEstoque", is(30)))
                .andExpect(jsonPath("$.categoriaId", is(categoriaHamburguer.getId().intValue())));
    }

    @Test
    @DisplayName("POST /api/admin/produtos - Deve retornar 400 Bad Request se preco for negativo ou zero")
    void deveRetornarErroQuandoPrecoForInvalido() throws Exception {
        ProdutoRequestDTO requestZero = new ProdutoRequestDTO(
                "X-Invalido",
                "Lanche",
                BigDecimal.ZERO,
                10,
                null,
                true,
                categoriaHamburguer.getId()
        );

        mockMvc.perform(post("/api/admin/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestZero)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/admin/produtos - Deve listar todos os produtos")
    void deveListarTodosOsProdutos() throws Exception {
        Produto p1 = new Produto("Produto 1", "Desc", new BigDecimal("10.00"), 5, categoriaHamburguer);
        Produto p2 = new Produto("Produto 2", "Desc", new BigDecimal("15.00"), 10, categoriaHamburguer);
        produtoRepository.save(p1);
        produtoRepository.save(p2);

        mockMvc.perform(get("/api/admin/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("PATCH /api/admin/produtos/{id}/estoque - Deve atualizar o estoque do produto")
    void deveAtualizarEstoqueComSucesso() throws Exception {
        Produto p = produtoRepository.save(new Produto("Produto Estocavel", "Desc", new BigDecimal("12.00"), 5, categoriaHamburguer));
        EstoqueUpdateDTO dto = new EstoqueUpdateDTO(100);

        mockMvc.perform(patch("/api/admin/produtos/{id}/estoque", p.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEstoque", is(100)));
    }

    @Test
    @DisplayName("DELETE /api/admin/produtos/{id} - Deve desativar o produto")
    void deveDesativarProduto() throws Exception {
        Produto p = produtoRepository.save(new Produto("Produto Desativavel", "Desc", new BigDecimal("12.00"), 5, categoriaHamburguer));

        mockMvc.perform(delete("/api/admin/produtos/{id}", p.getId()))
                .andExpect(status().isNoContent());

        Produto desativado = produtoRepository.findById(p.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertFalse(desativado.getAtivo());
    }
}
