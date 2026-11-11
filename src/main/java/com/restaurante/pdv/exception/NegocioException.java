package com.restaurante.pdv.exception;

/**
 * Exceção lançada quando uma regra de negócio for violada no sistema PDV.
 */
public class NegocioException extends RuntimeException {

    public NegocioException(String mensagem) {
        super(mensagem);
    }

    public NegocioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

