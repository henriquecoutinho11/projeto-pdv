package com.restaurante.pdv.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.pdv.dto.request.CustomizacaoItemDTO;
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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class TotemControllerIntegrationTest {

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

    private Produto produtoCombo;
    private OpcaoAdicional opcaoBacon;
    private OpcaoAdicional opcaoCheddar;
    private OpcaoAdicional opcaoSemCebola;
    private OpcaoAdicional opcaoBatataGrande;
    private OpcaoAdicional opcaoSucoLaranja;

    @BeforeEach
    void setUp() {
        Categoria cat = categoriaRepository.save(new Categoria("Combos Totem", "Categoria de teste"));

        opcaoBacon = opcaoAdicionalRepository.save(new OpcaoAdicional("Bacon Extra Totem", "Bacon crocante", new BigDecimal("3.50"), TipoOpcao.ADICIONAL, false, true));
        opcaoCheddar = opcaoAdicionalRepository.save(new OpcaoAdicional("Cheddar Extra Totem", "Cheddar cremoso", new BigDecimal("3.00"), TipoOpcao.ADICIONAL, false, true));
        opcaoSemCebola = opcaoAdicionalRepository.save(new OpcaoAdicional("Cebola Totem", "Cebola fresca", BigDecimal.ZERO, TipoOpcao.REMOCAO, true, true));
        opcaoBatataGrande = opcaoAdicionalRepository.save(new OpcaoAdicional("Batata Grande Totem", "Upgrade", new BigDecimal("4.00"), TipoOpcao.TAMANHO_BATATA, false, true));
        opcaoSucoLaranja = opcaoAdicionalRepository.save(new OpcaoAdicional("Suco Laranja Totem", "Suco 500ml", new BigDecimal("2.50"), TipoOpcao.BEBIDA, false, true));

        produtoCombo = new Produto("Combo Especial Totem", "Combo completo", new BigDecimal("40.00"), 50, cat);
        produtoCombo.adicionarOpcao(opcaoBacon);
        produtoCombo.adicionarOpcao(opcaoCheddar);
        produtoCombo.adicionarOpcao(opcaoSemCebola);
        produtoCombo.adicionarOpcao(opcaoBatataGrande);
        produtoCombo.adicionarOpcao(opcaoSucoLaranja);
        produtoCombo = produtoRepository.save(produtoCombo);
    }

    @Test
    @DisplayName("GET /api/totem/produtos/{id}/opcoes - Deve retornar as opções disponíveis do produto")
    void deveRetornarOpcoesDoProduto() throws Exception {
        mockMvc.perform(get("/api/totem/produtos/{produtoId}/opcoes", produtoCombo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[*].nome", hasItem("Bacon Extra Totem")))
                .andExpect(jsonPath("$[*].nome", hasItem("Batata Grande Totem")));
    }

    @Test
    @DisplayName("POST /api/totem/calcular-item - Deve calcular customização completa McDonald's Style")
    void deveCalcularItemCustomizadoNoTotem() throws Exception {
        CustomizacaoItemDTO request = new CustomizacaoItemDTO();
        request.setProdutoId(produtoCombo.getId());
        request.setQuantidade(2);
        request.setAdicionaisIds(List.of(opcaoBacon.getId())); // +3.50
        request.setTamanhoBatataId(opcaoBatataGrande.getId()); // +4.00
        request.setBebidaId(opcaoSucoLaranja.getId()); // +2.50
        request.setRemocoesIds(List.of(opcaoSemCebola.getId())); // R$ 0.00
        request.setObservacoes("Sem gelo na bebida");

        // Preço base = 40.00
        // Adicionais = 3.50 (bacon) + 2.50 (suco) = 6.00
        // Diferença tamanho = 4.00 (batata grande)
        // Unitário = 40.00 + 6.00 + 4.00 = 50.00
        // Subtotal = 50.00 * 2 = 100.00

        mockMvc.perform(post("/api/totem/calcular-item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoId", is(produtoCombo.getId().intValue())))
                .andExpect(jsonPath("$.precoBaseProduto", is(40.00)))
                .andExpect(jsonPath("$.somaAdicionais", is(6.00)))
                .andExpect(jsonPath("$.diferencaTamanho", is(4.00)))
                .andExpect(jsonPath("$.precoUnitario", is(50.00)))
                .andExpect(jsonPath("$.quantidade", is(2)))
                .andExpect(jsonPath("$.subtotal", is(100.00)))
                .andExpect(jsonPath("$.observacoes", containsString("Sem Cebola Totem")))
                .andExpect(jsonPath("$.observacoes", containsString("Obs: Sem gelo na bebida")))
                .andExpect(jsonPath("$.opcoes", hasSize(4)));
    }

    @Test
    @DisplayName("POST /api/pedidos/calcular-item - Deve pré-calcular item customizado via PedidoController")
    void deveCalcularItemViaPedidoController() throws Exception {
        CustomizacaoItemDTO request = new CustomizacaoItemDTO();
        request.setProdutoId(produtoCombo.getId());
        request.setQuantidade(1);
        request.setAdicionaisIds(List.of(opcaoCheddar.getId())); // +3.00

        mockMvc.perform(post("/api/pedidos/calcular-item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precoBaseProduto", is(40.00)))
                .andExpect(jsonPath("$.precoUnitario", is(43.00)))
                .andExpect(jsonPath("$.subtotal", is(43.00)));
    }
}

