package com.restaurante.pdv.model.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Serviço de conveniência/alias para gerador de senhas diárias conforme AGENTS.md.
 * Delega as operações para SenhaSequencialService.
 */
@Service
public class SenhaDiariaService {

    private final SenhaSequencialService senhaSequencialService;

    public SenhaDiariaService(SenhaSequencialService senhaSequencialService) {
        this.senhaSequencialService = senhaSequencialService;
    }

    public Integer gerarProximaSenha() {
        return senhaSequencialService.gerarProximaSenha();
    }

    public Integer gerarProximaSenha(LocalDate data) {
        return senhaSequencialService.gerarProximaSenha(data);
    }

    public String formatarSenha(Integer numeroSenha) {
        return SenhaSequencialService.formatarSenha(numeroSenha);
    }

    public void reiniciarSequencia() {
        senhaSequencialService.reiniciarSequencia();
    }
}
