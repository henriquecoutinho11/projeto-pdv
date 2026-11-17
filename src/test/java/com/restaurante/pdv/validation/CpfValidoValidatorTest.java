package com.restaurante.pdv.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CpfValidoValidatorTest {

    @Test
    @DisplayName("Deve validar CPF nulo como válido (campo opcional)")
    void deveAceitarCpfNulo() {
        assertTrue(CpfValidoValidator.isCpfValido(null));
    }

    @Test
    @DisplayName("Deve validar CPF vazio ou em branco como válido (campo opcional)")
    void deveAceitarCpfVazioOuEmBranco() {
        assertTrue(CpfValidoValidator.isCpfValido(""));
        assertTrue(CpfValidoValidator.isCpfValido("   "));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "52998224725",
            "529.982.247-25",
            "11144477735",
            "111.444.777-35"
    })
    @DisplayName("Deve aceitar CPFs válidos com ou sem formatação")
    void deveAceitarCpfsValidos(String cpf) {
        assertTrue(CpfValidoValidator.isCpfValido(cpf));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "00000000000",
            "11111111111",
            "12345678900",
            "123",
            "123.456.789-99",
            "abc.def.ghi-jk"
    })
    @DisplayName("Deve rejeitar CPFs inválidos ou malformados")
    void deveRejeitarCpfsInvalidos(String cpf) {
        assertFalse(CpfValidoValidator.isCpfValido(cpf));
    }

    @Test
    @DisplayName("Deve formatar corretamente CPF com 11 dígitos")
    void deveFormatarCpf() {
        assertEquals("529.982.247-25", CpfValidoValidator.formatarCpf("52998224725"));
        assertEquals("529.982.247-25", CpfValidoValidator.formatarCpf("529.982.247-25"));
        assertNull(CpfValidoValidator.formatarCpf(null));
        assertNull(CpfValidoValidator.formatarCpf("  "));
    }
}
