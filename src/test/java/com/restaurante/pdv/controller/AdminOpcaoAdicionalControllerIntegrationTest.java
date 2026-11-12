package com.restaurante.pdv.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.pdv.dto.request.OpcaoAdicionalRequestDTO;
import com.restaurante.pdv.model.entity.Categoria;
import com.restaurante.pdv.model.entity.OpcaoAdicional;
import com.restaurante.pdv.model.entity.Produto;
import com.restaurante.pdv.model.entity.TipoOpcao;
import com.restaurante.pdv.model.repository.CategoriaRepository;
import com.restaurante.pdv.model.repository.OpcaoAdicionalRepository;
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
class AdminOpcaoAdicionalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OpcaoAdicionalRepository opcaoAdicionalRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Produto produto;
    private OpcaoAdicional opcao;

    @BeforeEach
    void setUp() {
        Categoria cat = categoriaRepository.save(new Categoria("Lanches Admin", "Desc"));
        produto = produtoRepository.save(new Produto("Burguer Admin", "Desc", new BigDecimal("25.00"), 50, cat));
        opcao = opcaoAdicionalRepository.save(new OpcaoAdicional("Bacon Admin", "Desc", new BigDecimal("3.50"), TipoOpcao.ADICIONAL, false, true));
    }

    @Test
    @DisplayName("GET /api/admin/opcoes - Deve listar todas as opções")
    void deveListarTodasOpcoes() throws Exception {
        mockMvc.perform(get("/api/admin/opcoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("POST /api/admin/opcoes - Deve cadastrar nova opção com sucesso")
    void deveCadastrarNovaOpcao() throws Exception {
        OpcaoAdicionalRequestDTO request = new OpcaoAdicionalRequestDTO(
                "Cebola Crispy Extra",
                "Cebola frita crocante",
                new BigDecimal("4.50"),
                TipoOpcao.ADICIONAL,
                false,
                true
        );

        mockMvc.perform(post("/api/admin/opcoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nome", is("Cebola Crispy Extra")))
                .andExpect(jsonPath("$.precoExtra", is(4.50)))
                .andExpect(jsonPath("$.tipo", is("ADICIONAL")));
    }

    @Test
    @DisplayName("PUT /api/admin/opcoes/{id} - Deve atualizar opção existente")
    void deveAtualizarOpcao() throws Exception {
        OpcaoAdicionalRequestDTO request = new OpcaoAdicionalRequestDTO(
                "Bacon Super Crocante",
                "Nova descrição",
                new BigDecimal("4.00"),
                TipoOpcao.ADICIONAL,
                false,
                true
        );

        mockMvc.perform(put("/api/admin/opcoes/{id}", opcao.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Bacon Super Crocante")))
                .andExpect(jsonPath("$.precoExtra", is(4.00)));
    }

    @Test
    @DisplayName("DELETE /api/admin/opcoes/{id} - Deve desativar logicamente a opção")
    void deveDesativarOpcao() throws Exception {
        mockMvc.perform(delete("/api/admin/opcoes/{id}", opcao.getId()))
                .andExpect(status().isNoContent());

        OpcaoAdicional desativada = opcaoAdicionalRepository.findById(opcao.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertFalse(desativada.getAtivo());
    }

    @Test
    @DisplayName("POST /api/admin/opcoes/produtos/{prodId}/vincular/{opId} - Deve vincular opção ao produto")
    void deveVincularOpcaoAoProduto() throws Exception {
        mockMvc.perform(post("/api/admin/opcoes/produtos/{produtoId}/vincular/{opcaoId}", produto.getId(), opcao.getId()))
                .andExpect(status().isOk());

        Produto atualizado = produtoRepository.findById(produto.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(atualizado.getOpcoesDisponiveis().contains(opcao));
    }
}

