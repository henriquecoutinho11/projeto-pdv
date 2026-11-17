package com.restaurante.pdv.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador customizado de CPF para notas fiscais.
 * O campo permanece opcional para o cliente (nulo ou em branco é válido),
 * porém quando preenchido, valida rigidez de dígitos e algoritmo oficial.
 */
public class CpfValidoValidator implements ConstraintValidator<CpfValido, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return isCpfValido(value);
    }

    /**
     * Valida se uma string representa um CPF válido ou nulo/vazio.
     *
     * @param cpfString CPF formatado (ex: 000.000.000-00) ou apenas dígitos.
     * @return true se nulo, em branco ou se possui dígitos verificadores válidos; false caso contrário.
     */
    public static boolean isCpfValido(String cpfString) {
        if (cpfString == null || cpfString.trim().isEmpty()) {
            return true;
        }

        String cpf = cpfString.replaceAll("\\D", "");

        if (cpf.length() != 11) {
            return false;
        }

        // Rejeita sequências de dígitos repetidos (ex: 00000000000, 11111111111)
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
            int soma1 = 0;
            for (int i = 0; i < 9; i++) {
                soma1 += (cpf.charAt(i) - '0') * (10 - i);
            }
            int digito1 = 11 - (soma1 % 11);
            if (digito1 >= 10) {
                digito1 = 0;
            }

            if (digito1 != (cpf.charAt(9) - '0')) {
                return false;
            }

            int soma2 = 0;
            for (int i = 0; i < 10; i++) {
                soma2 += (cpf.charAt(i) - '0') * (11 - i);
            }
            int digito2 = 11 - (soma2 % 11);
            if (digito2 >= 10) {
                digito2 = 0;
            }

            return digito2 == (cpf.charAt(10) - '0');
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Formata uma string de 11 dígitos no padrão de CPF (000.000.000-00).
     *
     * @param cpfString String original do CPF
     * @return String formatada ou a original se não tiver 11 dígitos
     */
    public static String formatarCpf(String cpfString) {
        if (cpfString == null || cpfString.trim().isEmpty()) {
            return null;
        }
        String numeros = cpfString.replaceAll("\\D", "");
        if (numeros.length() == 11) {
            return String.format("%s.%s.%s-%s",
                    numeros.substring(0, 3),
                    numeros.substring(3, 6),
                    numeros.substring(6, 9),
                    numeros.substring(9, 11));
        }
        return cpfString;
    }
}
