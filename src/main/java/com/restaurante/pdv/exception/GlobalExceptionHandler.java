package com.restaurante.pdv.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Tratador global de exceções conforme especificação RFC 7807 (ProblemDetail).
 * Centraliza e padroniza os erros retornados pela API e Controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String BASE_URI = "https://pdv.antuneslanches.com.br/erros/";

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Recurso Não Encontrado");
        problemDetail.setType(URI.create(BASE_URI + "recurso-nao-encontrado"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(NegocioException.class)
    public ProblemDetail handleNegocio(NegocioException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Regra de Negócio Violada");
        problemDetail.setType(URI.create(BASE_URI + "regra-de-negocio"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ProblemDetail problemDetail = ex.getBody();
        problemDetail.setTitle("Dados Inválidos");
        problemDetail.setType(URI.create(BASE_URI + "dados-invalidos"));
        problemDetail.setDetail("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.");
        problemDetail.setProperty("timestamp", Instant.now());

        Map<String, String> errosCampos = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errosCampos.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problemDetail.setProperty("campos", errosCampos);

        return ResponseEntity.status(status).headers(headers).body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno inesperado no servidor. Tente novamente mais tarde."
        );
        problemDetail.setTitle("Erro Interno do Servidor");
        problemDetail.setType(URI.create(BASE_URI + "erro-interno"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
