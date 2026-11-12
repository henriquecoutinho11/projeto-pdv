package com.restaurante.pdv.exception;

/**
 * Exceção lançada quando uma entidade ou recurso solicitado não for encontrado.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public RecursoNaoEncontradoException(String entidade, Object id) {
        super(String.format("%s com identificador '%s' não foi encontrado(a).", entidade, id));
    }
}

