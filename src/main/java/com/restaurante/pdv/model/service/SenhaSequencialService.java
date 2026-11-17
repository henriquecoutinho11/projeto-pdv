package com.restaurante.pdv.model.service;

import com.restaurante.pdv.model.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Serviço gerador de senhas sequenciais diárias para os pedidos do PDV (estilo McDonald's).
 * Garante controle de concorrência thread-safe e reinício automático da numeração em 1 a cada virada de dia (00:00h).
 */
@Service
public class SenhaSequencialService {

    private final PedidoRepository pedidoRepository;
    private final AtomicInteger contador = new AtomicInteger(0);
    private LocalDate ultimaData = null;

    public SenhaSequencialService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Gera a próxima senha sequencial para a data atual.
     * Thread-safe e reinicia em 1 a cada novo dia civil.
     *
     * @return Número inteiro da senha gerada (1, 2, 3...)
     */
    public synchronized Integer gerarProximaSenha() {
        return gerarProximaSenha(LocalDate.now());
    }

    /**
     * Gera a próxima senha sequencial para uma data informada.
     * Permite injeção de datas customizadas para testes e controle diário.
     *
     * @param data Data civil de referência
     * @return Número inteiro da senha gerada
     */
    public synchronized Integer gerarProximaSenha(LocalDate data) {
        LocalDate dataAlvo = data != null ? data : LocalDate.now();

        if (ultimaData == null || !dataAlvo.equals(ultimaData)) {
            LocalDateTime inicio = dataAlvo.atStartOfDay();
            LocalDateTime fim = dataAlvo.atTime(LocalTime.MAX);

            Integer maxSenhaBanco = null;
            if (pedidoRepository != null) {
                maxSenhaBanco = pedidoRepository.findMaxNumeroSenhaByDataHoraBetween(inicio, fim).orElse(0);
            }

            int inicioContador = (maxSenhaBanco != null ? maxSenhaBanco : 0) + 1;
            contador.set(inicioContador);
            ultimaData = dataAlvo;
        }

        return contador.getAndIncrement();
    }

    /**
     * Formata o número da senha em representação textual padrão para o totem/painel.
     * Exemplo: 1 -> "#001", 42 -> "#042", 100 -> "#100".
     *
     * @param numeroSenha Número inteiro da senha
     * @return String formatada com hash e 3 dígitos
     */
    public static String formatarSenha(Integer numeroSenha) {
        if (numeroSenha == null) {
            return null;
        }
        return String.format("#%03d", numeroSenha);
    }

    /**
     * Reinicia manualmente a sequência para a data atual (útil em testes unitários).
     */
    public synchronized void reiniciarSequencia() {
        this.contador.set(1);
        this.ultimaData = LocalDate.now();
    }
}
