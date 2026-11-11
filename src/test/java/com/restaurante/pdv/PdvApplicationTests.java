package com.restaurante.pdv;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class PdvApplicationTests {

    @Test
    @DisplayName("Deve carregar o contexto da aplicação Spring Boot e conexões com sucesso")
    void contextLoads() {
        // Valida que todos os beans essenciais, DataSource H2 e JPA inicializam sem falhas
    }
}

