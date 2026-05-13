package com.arenapernambuco.controller;

import com.arenapernambuco.dto.EventoDTO;
import com.arenapernambuco.dto.IngressoDTO;
import com.arenapernambuco.service.EventoService;
import com.arenapernambuco.service.IngressoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class VerificacaoController {

    private static final int CODIGO_MAX_LENGTH = 20;

    private final EventoService eventoService;
    private final IngressoService ingressoService;

    public VerificacaoController(EventoService eventoService, IngressoService ingressoService) {
        this.eventoService = eventoService;
        this.ingressoService = ingressoService;
    }

    @GetMapping("/verificar")
    public String formulario() {
        return "verificar";
    }

    @PostMapping("/verificar")
    public String verificar(@RequestParam(required = false) String codigo, Model model) {
        model.addAttribute("submitted", true);
        String codigoLimpo = sanitizarCodigo(codigo);

        if (codigoLimpo.isEmpty()) {
            model.addAttribute("tipo", "vazio");
            return "verificar";
        }

        // Tenta código de evento primeiro
        Optional<EventoDTO> evento = eventoService.verificarPorCodigo(codigoLimpo);
        if (evento.isPresent()) {
            model.addAttribute("evento", evento.get());
            model.addAttribute("tipo", evento.get().ativo() ? "evento_ativo" : "evento_inativo");
            return "verificar";
        }

        // Tenta código de ingresso
        Optional<IngressoDTO> ingresso = ingressoService.buscarPorCodigo(codigoLimpo);
        if (ingresso.isPresent()) {
            model.addAttribute("ingresso", ingresso.get());
            model.addAttribute("tipo", ingresso.get().cancelado() ? "ingresso_cancelado" : "ingresso_ativo");
            return "verificar";
        }

        model.addAttribute("tipo", "nao_encontrado");
        model.addAttribute("codigoInformado", codigoLimpo);
        return "verificar";
    }

    private String sanitizarCodigo(String codigo) {
        if (codigo == null) return "";
        String limpo = codigo.trim().toUpperCase();
        if (limpo.length() > CODIGO_MAX_LENGTH) {
            limpo = limpo.substring(0, CODIGO_MAX_LENGTH);
        }
        if (!limpo.matches("[A-Z0-9\\-]+")) return "";
        return limpo;
    }
}
