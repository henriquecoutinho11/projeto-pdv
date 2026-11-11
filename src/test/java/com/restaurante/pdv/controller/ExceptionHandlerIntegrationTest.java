package com.restaurante.pdv.controller;

import com.restaurante.pdv.exception.NegocioException;
import com.restaurante.pdv.exception.RecursoNaoEncontradoException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ExceptionHandlerIntegrationTest.TesteErroController.class)
@ActiveProfiles("dev")
@DisplayName("Testes de Integração de Erros HTTP (RFC 7807 ProblemDetail)")
class ExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    record ExemploRequestDTO(@NotBlank(message = "O nome é obrigatório") String nome) {}

    @RestController
    @RequestMapping("/test-erros")
    static class TesteErroController {

        @GetMapping("/negocio")
        public void disparaNegocio() {
            throw new NegocioException("Erro de negócio de teste");
        }

        @GetMapping("/nao-encontrado")
        public void disparaNaoEncontrado() {
            throw new RecursoNaoEncontradoException("Produto", 42L);
        }

        @PostMapping("/validacao")
        public void disparaValidacao(@Valid @RequestBody ExemploRequestDTO dto) {
            // Se passar na validação, não faz nada
        }
    }

    @Test
    @DisplayName("Deve retornar status 400 e ProblemDetail RFC 7807 quando NegocioException é lançada")
    void deveRetornarProblemDetailNegocio() throws Exception {
        mockMvc.perform(get("/test-erros/negocio").accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Regra de Negócio Violada"))
                .andExpect(jsonPath("$.detail").value("Erro de negócio de teste"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Deve retornar status 404 e ProblemDetail RFC 7807 quando RecursoNaoEncontradoException é lançada")
    void deveRetornarProblemDetailNaoEncontrado() throws Exception {
        mockMvc.perform(get("/test-erros/nao-encontrado").accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso Não Encontrado"))
                .andExpect(jsonPath("$.detail").value("Produto com identificador '42' não foi encontrado(a)."))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Deve retornar status 400 e ProblemDetail com campos quando validação de DTO falha")
    void deveRetornarProblemDetailValidacao() throws Exception {
        mockMvc.perform(post("/test-erros/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\"}")
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados Inválidos"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos.nome").value("O nome é obrigatório"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
