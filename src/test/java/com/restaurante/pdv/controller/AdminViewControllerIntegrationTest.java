package com.restaurante.pdv.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@DisplayName("Testes de Integração do Painel Administrativo (AdminViewController)")
class AdminViewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /admin - Deve renderizar o painel administrativo com atributos no model")
    void deveRenderizarPainelAdminComModel() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/index"))
                .andExpect(model().attributeExists("categorias"))
                .andExpect(model().attributeExists("produtos"))
                .andExpect(model().attributeExists("resumo"))
                .andExpect(model().attribute("categorias", notNullValue()))
                .andExpect(model().attribute("produtos", notNullValue()))
                .andExpect(model().attribute("resumo", notNullValue()));
    }

    @Test
    @DisplayName("GET /admin/dashboard - Deve renderizar a rota alternativa do dashboard")
    void deveRenderizarRotaDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/index"));
    }

    @Test
    @DisplayName("GET /admin/produtos - Deve renderizar a rota alternativa de produtos")
    void deveRenderizarRotaProdutos() throws Exception {
        mockMvc.perform(get("/admin/produtos"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/index"));
    }
}
