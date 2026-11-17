package com.restaurante.pdv.service;

import com.restaurante.pdv.model.repository.PedidoRepository;
import com.restaurante.pdv.model.service.SenhaSequencialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SenhaSequencialServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    private SenhaSequencialService senhaSequencialService;

    @BeforeEach
    void setUp() {
        senhaSequencialService = new SenhaSequencialService(pedidoRepository);
    }

    @Test
    @DisplayName("Deve gerar senhas sequenciais crescentes a partir de 1")
    void deveGerarSenhasSequenciais() {
        when(pedidoRepository.findMaxNumeroSenhaByDataHoraBetween(any(), any()))
                .thenReturn(Optional.empty());

        Integer s1 = senhaSequencialService.gerarProximaSenha();
        Integer s2 = senhaSequencialService.gerarProximaSenha();
        Integer s3 = senhaSequencialService.gerarProximaSenha();

        assertEquals(1, s1);
        assertEquals(2, s2);
        assertEquals(3, s3);
    }

    @Test
    @DisplayName("Deve continuar a sequência a partir do maior número existente no banco para o dia")
    void deveContinuarSequenciaDoBanco() {
        when(pedidoRepository.findMaxNumeroSenhaByDataHoraBetween(any(), any()))
                .thenReturn(Optional.of(41));

        Integer s1 = senhaSequencialService.gerarProximaSenha();
        Integer s2 = senhaSequencialService.gerarProximaSenha();

        assertEquals(42, s1);
        assertEquals(43, s2);
    }

    @Test
    @DisplayName("Deve reiniciar a contagem em 1 na virada de dia")
    void deveReiniciarContagemNaViradaDeDia() {
        LocalDate hoje = LocalDate.of(2026, 11, 17);
        LocalDate amanha = LocalDate.of(2026, 11, 18);

        when(pedidoRepository.findMaxNumeroSenhaByDataHoraBetween(any(), any()))
                .thenReturn(Optional.empty());

        Integer s1 = senhaSequencialService.gerarProximaSenha(hoje);
        Integer s2 = senhaSequencialService.gerarProximaSenha(hoje);
        assertEquals(1, s1);
        assertEquals(2, s2);

        // Virada de dia
        Integer sAmanha1 = senhaSequencialService.gerarProximaSenha(amanha);
        Integer sAmanha2 = senhaSequencialService.gerarProximaSenha(amanha);

        assertEquals(1, sAmanha1);
        assertEquals(2, sAmanha2);
    }

    @Test
    @DisplayName("Deve garantir concorrência thread-safe sem gerar senhas duplicadas")
    void deveGarantirThreadSafety() throws Exception {
        when(pedidoRepository.findMaxNumeroSenhaByDataHoraBetween(any(), any()))
                .thenReturn(Optional.empty());

        int totalThreads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(1);
        Set<Integer> senhasGeradas = Collections.synchronizedSet(new HashSet<>());

        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < totalThreads; i++) {
            futures.add(executor.submit(() -> {
                latch.await();
                return senhaSequencialService.gerarProximaSenha();
            }));
        }

        latch.countDown(); // Libera todas as threads simultaneamente

        for (Future<Integer> f : futures) {
            senhasGeradas.add(f.get());
        }

        executor.shutdown();

        assertEquals(totalThreads, senhasGeradas.size(), "Todas as senhas geradas simultaneamente devem ser únicas");
    }

    @Test
    @DisplayName("Deve formatar o número da senha corretamente (#001, #042)")
    void deveFormatarSenhaCorretamente() {
        assertEquals("#001", SenhaSequencialService.formatarSenha(1));
        assertEquals("#042", SenhaSequencialService.formatarSenha(42));
        assertEquals("#999", SenhaSequencialService.formatarSenha(999));
        assertEquals("#1000", SenhaSequencialService.formatarSenha(1000));
        assertNull(SenhaSequencialService.formatarSenha(null));
    }
}
