package com.restaurante.pdv.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.pdv.dto.request.AtualizarStatusKdsDTO;
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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@DisplayName("Testes de Integração da API do KDS (Kitchen Display System)")
class KdsControllerIntegrationTest {

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
    private OpcaoAdicional bacon;
    private OpcaoAdicional cebola;

    @BeforeEach
    void setUp() {
        Categoria cat = categoriaRepository.save(new Categoria("Lanches KDS", "Lanches da cozinha"));

        bacon = opcaoAdicionalRepository.save(new OpcaoAdicional(
                "Bacon Extra", "Fatias de bacon crocante",
                new BigDecimal("4.00"), TipoOpcao.ADICIONAL, false, true
        ));

        cebola = opcaoAdicionalRepository.save(new OpcaoAdicional(
                "Cebola", "Cebola fresca",
                BigDecimal.ZERO, TipoOpcao.REMOCAO, true, true
        ));

        burguer = new Produto(
                null, "X-Burger KDS", "Lanche especial da cozinha",
                new BigDecimal("28.00"), 50, "http://img.com/burguer.png", true, cat
        );
        burguer.setOpcoesDisponiveis(new HashSet<>(Set.of(bacon, cebola)));
        burguer = produtoRepository.save(burguer);
    }

    private Pedido criarPedidoComItens(StatusPedido status, boolean pago, LocalDateTime dataHora, Integer senha) {
        Pedido pedido = new Pedido();
        pedido.setStatus(status);
        pedido.setPago(pago);
        pedido.setDataHora(dataHora);
        pedido.setNumeroSenha(senha);
        pedido.setNomeCliente("Cliente KDS");
        pedido.setFormaPagamento(FormaPagamento.PIX);

        ItemPedido item = new ItemPedido(burguer, 1, new BigDecimal("32.00"), new BigDecimal("32.00"), "Sem cebola");
        ItemPedidoOpcao opRemocao = new ItemPedidoOpcao(cebola, "Sem cebola", BigDecimal.ZERO, TipoOpcao.REMOCAO);
        ItemPedidoOpcao opAdicional = new ItemPedidoOpcao(bacon, "Bacon Extra", new BigDecimal("4.00"), TipoOpcao.ADICIONAL);

        item.adicionarOpcao(opRemocao);
        item.adicionarOpcao(opAdicional);
        pedido.adicionarItem(item);

        return pedidoRepository.save(pedido);
    }

    @Test
    @DisplayName("GET /api/kds/pedidos-ativos - Deve listar apenas pedidos RECEBIDO e EM_PREPARO ordenados por tempo de espera")
    void deveListarPedidosAtivosOrdenadosPorTempoEspera() throws Exception {
        LocalDateTime agora = LocalDateTime.now();

        // Pedido 1: mais antigo (15 minutos atrás -> alerta VERMELHO)
        Pedido p1 = criarPedidoComItens(StatusPedido.RECEBIDO, true, agora.minusMinutes(15), 101);
        // Pedido 2: intermediário (7 minutos atrás -> alerta AMARELO)
        Pedido p2 = criarPedidoComItens(StatusPedido.EM_PREPARO, true, agora.minusMinutes(7), 102);
        // Pedido 3: recente (2 minutos atrás -> alerta VERDE)
        Pedido p3 = criarPedidoComItens(StatusPedido.RECEBIDO, true, agora.minusMinutes(2), 103);

        // Pedidos inativos para a tela da cozinha (não devem aparecer no KDS ativo)
        criarPedidoComItens(StatusPedido.PRONTO, true, agora.minusMinutes(20), 99);
        criarPedidoComItens(StatusPedido.ENTREGUE, true, agora.minusMinutes(25), 98);
        criarPedidoComItens(StatusPedido.CANCELADO, false, agora.minusMinutes(30), 97);

        mockMvc.perform(get("/api/kds/pedidos-ativos")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                // Validação de ordenação: mais antigo primeiro (tempo de espera decrescente)
                .andExpect(jsonPath("$[0].id").value(p1.getId()))
                .andExpect(jsonPath("$[0].numeroSenha").value(101))
                .andExpect(jsonPath("$[0].status").value("RECEBIDO"))
                .andExpect(jsonPath("$[0].proximoStatus").value("EM_PREPARO"))
                .andExpect(jsonPath("$[0].alertaSla").value("VERMELHO"))
                .andExpect(jsonPath("$[0].itens[0].tags", hasItem("SEM CEBOLA")))
                .andExpect(jsonPath("$[0].itens[0].tags", hasItem("+ BACON EXTRA")))

                .andExpect(jsonPath("$[1].id").value(p2.getId()))
                .andExpect(jsonPath("$[1].numeroSenha").value(102))
                .andExpect(jsonPath("$[1].status").value("EM_PREPARO"))
                .andExpect(jsonPath("$[1].proximoStatus").value("PRONTO"))
                .andExpect(jsonPath("$[1].alertaSla").value("AMARELO"))

                .andExpect(jsonPath("$[2].id").value(p3.getId()))
                .andExpect(jsonPath("$[2].numeroSenha").value(103))
                .andExpect(jsonPath("$[2].status").value("RECEBIDO"))
                .andExpect(jsonPath("$[2].proximoStatus").value("EM_PREPARO"))
                .andExpect(jsonPath("$[2].alertaSla").value("VERDE"));
    }

    @Test
    @DisplayName("PATCH /api/kds/pedidos/{id}/status - Deve avançar status automaticamente sem corpo")
    void deveAvancarStatusAutomaticamenteSemCorpo() throws Exception {
        Pedido pedido = criarPedidoComItens(StatusPedido.RECEBIDO, true, LocalDateTime.now(), 201);

        // 1. RECEBIDO -> EM_PREPARO
        mockMvc.perform(patch("/api/kds/pedidos/{id}/status", pedido.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pedido.getId()))
                .andExpect(jsonPath("$.status").value("EM_PREPARO"))
                .andExpect(jsonPath("$.proximoStatus").value("PRONTO"));

        // 2. EM_PREPARO -> PRONTO
        mockMvc.perform(patch("/api/kds/pedidos/{id}/status", pedido.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PRONTO"))
                .andExpect(jsonPath("$.proximoStatus").value("ENTREGUE"));

        // 3. PRONTO -> ENTREGUE
        mockMvc.perform(patch("/api/kds/pedidos/{id}/status", pedido.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE"))
                .andExpect(jsonPath("$.proximoStatus").doesNotExist());
    }

    @Test
    @DisplayName("PATCH /api/kds/pedidos/{id}/status - Deve avançar com status explicitado no corpo")
    void deveAvancarStatusComCorpoJson() throws Exception {
        Pedido pedido = criarPedidoComItens(StatusPedido.RECEBIDO, true, LocalDateTime.now(), 202);

        AtualizarStatusKdsDTO body = new AtualizarStatusKdsDTO(StatusPedido.EM_PREPARO);

        mockMvc.perform(patch("/api/kds/pedidos/{id}/status", pedido.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_PREPARO"));
    }

    @Test
    @DisplayName("PATCH /api/kds/pedidos/{id}/status - Deve retornar 422 Unprocessable Entity ao tentar pular etapas (RECEBIDO -> ENTREGUE)")
    void deveRetornar422AoPularEtapas() throws Exception {
        Pedido pedido = criarPedidoComItens(StatusPedido.RECEBIDO, true, LocalDateTime.now(), 203);

        AtualizarStatusKdsDTO body = new AtualizarStatusKdsDTO(StatusPedido.ENTREGUE);

        mockMvc.perform(patch("/api/kds/pedidos/{id}/status", pedido.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Transição de Status Inválida"))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.statusAtual").value("RECEBIDO"))
                .andExpect(jsonPath("$.statusDestino").value("ENTREGUE"))
                .andExpect(jsonPath("$.detail", containsString("Transição de status inválida de 'RECEBIDO' para 'ENTREGUE'")));
    }

    @Test
    @DisplayName("PATCH /api/kds/pedidos/{id}/status - Deve retornar 422 Unprocessable Entity ao tentar pular etapa (RECEBIDO -> PRONTO)")
    void deveRetornar422AoPularEtapaParaPronto() throws Exception {
        Pedido pedido = criarPedidoComItens(StatusPedido.RECEBIDO, true, LocalDateTime.now(), 204);

        AtualizarStatusKdsDTO body = new AtualizarStatusKdsDTO(StatusPedido.PRONTO);

        mockMvc.perform(patch("/api/kds/pedidos/{id}/status", pedido.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.title").value("Transição de Status Inválida"));
    }

    @Test
    @DisplayName("PATCH /api/kds/pedidos/{id}/status - Deve retornar 422 Unprocessable Entity ao tentar avançar pedido já ENTREGUE")
    void deveRetornar422AoAvancarPedidoEntregue() throws Exception {
        Pedido pedido = criarPedidoComItens(StatusPedido.ENTREGUE, true, LocalDateTime.now(), 205);

        mockMvc.perform(patch("/api/kds/pedidos/{id}/status", pedido.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.title").value("Transição de Status Inválida"));
    }

    @Test
    @DisplayName("PATCH /api/kds/pedidos/{id}/status - Deve retornar 400 Bad Request ao tentar avançar pedido não pago para preparo")
    void deveRetornar400AoAvancarPedidoNaoPagoParaPreparo() throws Exception {
        Pedido pedido = criarPedidoComItens(StatusPedido.RECEBIDO, false, LocalDateTime.now(), 206);

        mockMvc.perform(patch("/api/kds/pedidos/{id}/status", pedido.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Regra de Negócio Violada"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail", containsString("não pode ter status alterado para preparo na cozinha antes da confirmação do pagamento")));
    }

    @Test
    @DisplayName("PATCH /api/kds/pedidos/{id}/status - Deve retornar 404 Not Found para pedido inexistente")
    void deveRetornar404ParaPedidoInexistente() throws Exception {
        mockMvc.perform(patch("/api/kds/pedidos/{id}/status", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso Não Encontrado"))
                .andExpect(jsonPath("$.status").value(404));
    }
}
