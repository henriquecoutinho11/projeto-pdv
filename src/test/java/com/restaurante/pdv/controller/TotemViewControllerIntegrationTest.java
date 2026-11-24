package com.restaurante.pdv.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.pdv.dto.request.ModalidadeConsumoDTO;
import com.restaurante.pdv.model.entity.TipoConsumo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class TotemViewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET / deve redirecionar para a rota do totem /totem")
    void deveRedirecionarRaizParaTotem() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/totem"));
    }

    @Test
    @DisplayName("GET /totem deve renderizar a view totem/index com status 200 OK e elementos do design system")
    void deveRenderizarViewTotemComSucesso() throws Exception {
        mockMvc.perform(get("/totem"))
                .andExpect(status().isOk())
                .andExpect(view().name("totem/index"))
                .andExpect(content().string(containsString("ANTUNES")))
                .andExpect(content().string(containsString("Bateu aquela fome?")))
                .andExpect(content().string(containsString("Para Comer Aqui")))
                .andExpect(content().string(containsString("Para Viagem")))
                .andExpect(content().string(containsString("totem.css")))
                .andExpect(content().string(containsString("totem.js")));
    }

    @Test
    @DisplayName("POST /totem/modalidade deve registrar COMER_AQUI na sessão com status 200 OK")
    void deveRegistrarModalidadeComerAquiNaSessao() throws Exception {
        MockHttpSession session = new MockHttpSession();
        ModalidadeConsumoDTO dto = new ModalidadeConsumoDTO(TipoConsumo.COMER_AQUI);

        mockMvc.perform(post("/totem/modalidade")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.tipoConsumo").value("COMER_AQUI"))
                .andExpect(jsonPath("$.descricao").value("Para Comer Aqui"));

        assertEquals(TipoConsumo.COMER_AQUI, session.getAttribute(TotemViewController.SESSION_TIPO_CONSUMO));
    }

    @Test
    @DisplayName("POST /totem/modalidade deve registrar PARA_VIAGEM na sessão com status 200 OK")
    void deveRegistrarModalidadeParaViagemNaSessao() throws Exception {
        MockHttpSession session = new MockHttpSession();
        ModalidadeConsumoDTO dto = new ModalidadeConsumoDTO(TipoConsumo.PARA_VIAGEM);

        mockMvc.perform(post("/totem/modalidade")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.tipoConsumo").value("PARA_VIAGEM"))
                .andExpect(jsonPath("$.descricao").value("Para Viagem"));

        assertEquals(TipoConsumo.PARA_VIAGEM, session.getAttribute(TotemViewController.SESSION_TIPO_CONSUMO));
    }

    @Test
    @DisplayName("POST /totem/modalidade com payload inválido deve retornar 400 Bad Request")
    void deveRetornar400QuandoModalidadeForNula() throws Exception {
        mockMvc.perform(post("/totem/modalidade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoConsumo\": null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /totem/reset deve limpar o estado da sessão do cliente")
    void deveResetarSessaoDoTotem() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(TotemViewController.SESSION_TIPO_CONSUMO, TipoConsumo.COMER_AQUI);

        mockMvc.perform(post("/totem/reset").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        assertNull(session.getAttribute(TotemViewController.SESSION_TIPO_CONSUMO));
    }

    @Test
    @DisplayName("GET /totem/sessao deve refletir se a sessão está ativa e a modalidade atual")
    void deveConsultarEstadoDaSessao() throws Exception {
        MockHttpSession session = new MockHttpSession();

        // Sessão vazia
        mockMvc.perform(get("/totem/sessao").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false))
                .andExpect(jsonPath("$.tipoConsumo").doesNotExist());

        // Sessão com modalidade
        session.setAttribute(TotemViewController.SESSION_TIPO_CONSUMO, TipoConsumo.PARA_VIAGEM);
        mockMvc.perform(get("/totem/sessao").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.tipoConsumo").value("PARA_VIAGEM"));
    }

    @Test
    @DisplayName("GET /totem/cardapio deve renderizar a vitrine do cardápio com categorias e produtos")
    void deveRenderizarCardapioDireto() throws Exception {
        mockMvc.perform(get("/totem/cardapio"))
                .andExpect(status().isOk())
                .andExpect(view().name("totem/index"))
                .andExpect(model().attributeExists("categorias"))
                .andExpect(model().attributeExists("produtos"))
                .andExpect(model().attribute("abrirCardapioDireto", true))
                .andExpect(content().string(containsString("Hambúrgueres Artesanais")))
                .andExpect(content().string(containsString("screen-menu")))
                .andExpect(content().string(containsString("modal-customizacao")));
    }

    @Test
    @DisplayName("POST /totem/identificacao deve registrar nomeCliente e cpfCliente na sessão com status 200 OK")
    void deveRegistrarIdentificacaoDoClienteNaSessao() throws Exception {
        MockHttpSession session = new MockHttpSession();
        java.util.Map<String, String> payload = new java.util.HashMap<>();
        payload.put("nomeCliente", "Carlos Antunes");
        payload.put("cpfCliente", "529.982.247-25");

        mockMvc.perform(post("/totem/identificacao")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.nomeCliente").value("Carlos Antunes"))
                .andExpect(jsonPath("$.cpfCliente").value("529.982.247-25"));

        assertEquals("Carlos Antunes", session.getAttribute(TotemViewController.SESSION_NOME_CLIENTE));
        assertEquals("529.982.247-25", session.getAttribute(TotemViewController.SESSION_CPF_CLIENTE));
    }

    @Test
    @DisplayName("POST /totem/reset deve limpar também nomeCliente e cpfCliente da sessão")
    void deveResetarSessaoELimparIdentificacao() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(TotemViewController.SESSION_TIPO_CONSUMO, TipoConsumo.COMER_AQUI);
        session.setAttribute(TotemViewController.SESSION_NOME_CLIENTE, "Carlos Antunes");
        session.setAttribute(TotemViewController.SESSION_CPF_CLIENTE, "529.982.247-25");

        mockMvc.perform(post("/totem/reset").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        assertNull(session.getAttribute(TotemViewController.SESSION_TIPO_CONSUMO));
        assertNull(session.getAttribute(TotemViewController.SESSION_NOME_CLIENTE));
        assertNull(session.getAttribute(TotemViewController.SESSION_CPF_CLIENTE));
    }

    @Test
    @DisplayName("GET /totem deve renderizar elementos do carrinho flutuante, drawer e tela de identificação touch")
    void deveRenderizarElementosDoCarrinhoEIdentificacaoNaView() throws Exception {
        mockMvc.perform(get("/totem"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("screen-identification")))
                .andExpect(content().string(containsString("floating-cart-bar")))
                .andExpect(content().string(containsString("cart-drawer-modal")))
                .andExpect(content().string(containsString("keyboard-qwerty")))
                .andExpect(content().string(containsString("keyboard-numpad")))
                .andExpect(content().string(containsString("btn-skip-cpf")));
    }
}
