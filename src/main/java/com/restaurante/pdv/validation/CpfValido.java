package com.restaurante.pdv.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação de validação customizada para validação de CPF.
 * Permite valores nulos ou vazios (campo opcional para notas fiscais),
 * mas valida a sintaxe e dígitos verificadores quando preenchido.
 */
@Documented
@Constraint(validatedBy = CpfValidoValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CpfValido {
    String message() default "O CPF informado é inválido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
