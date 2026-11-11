package com.restaurante.pdv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Ponto de entrada da aplicação Sistema PDV Web Core - Antunes Lanches.
 * Habilita a infraestrutura do Spring Boot e o gerenciamento transacional declarativo (@Transactional).
 */
@SpringBootApplication
@EnableTransactionManagement
public class PdvApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdvApplication.class, args);
    }
}

