package com.restaurante.pdv.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller MVC para renderização do Painel Operacional da Cozinha (KDS - Kitchen Display System).
 */
@Controller
public class KdsViewController {

    /**
     * Renderiza a tela do KDS da cozinha em tempo real.
     */
    @GetMapping("/kds")
    public String kds() {
        return "kds/index";
    }
}
