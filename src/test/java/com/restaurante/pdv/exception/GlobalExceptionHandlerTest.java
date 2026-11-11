package com.restaurante.pdv.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários do GlobalExceptionHandler (RFC 7807)")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Deve retornar ProblemDetail com status 404 para RecursoNaoEncontradoException")
    void deveTratarRecursoNaoEncontrado() {
        RecursoNaoEncontradoException exception = new RecursoNaoEncontradoException("Produto", 999L);
        ProblemDetail problem = exceptionHandler.handleRecursoNaoEncontrado(exception);

        assertNotNull(problem);
        assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
        assertEquals("Recurso Não Encontrado", problem.getTitle());
        assertTrue(problem.getDetail().contains("Produto com identificador '999' não foi encontrado(a)"));
        assertNotNull(problem.getProperties().get("timestamp"));
    }

    @Test
    @DisplayName("Deve retornar ProblemDetail com status 400 para NegocioException")
    void deveTratarNegocioException() {
        NegocioException exception = new NegocioException("Estoque insuficiente para o produto informado.");
        ProblemDetail problem = exceptionHandler.handleNegocio(exception);

        assertNotNull(problem);
        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Regra de Negócio Violada", problem.getTitle());
        assertEquals("Estoque insuficiente para o produto informado.", problem.getDetail());
        assertNotNull(problem.getProperties().get("timestamp"));
    }

    @Test
    @DisplayName("Deve retornar ProblemDetail com status 500 para exceções genéricas")
    void deveTratarExcecaoGenerica() {
        Exception exception = new RuntimeException("Erro inesperado");
        ProblemDetail problem = exceptionHandler.handleGenericException(exception);

        assertNotNull(problem);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problem.getStatus());
        assertEquals("Erro Interno do Servidor", problem.getTitle());
        assertTrue(problem.getDetail().contains("Ocorreu um erro interno inesperado"));
        assertNotNull(problem.getProperties().get("timestamp"));
    }
}

