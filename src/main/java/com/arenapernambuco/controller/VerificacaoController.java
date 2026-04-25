package com.arenapernambuco.controller;

import com.arenapernambuco.dto.EventoDTO;
import com.arenapernambuco.service.EventoService;
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

    public VerificacaoController(EventoService eventoService) {
        this.eventoService = eventoService;
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
            model.addAttribute("ok", false);
            model.addAttribute("codigoInformado", "");
            return "verificar";
        }

        Optional<EventoDTO> encontrado = eventoService.verificarPorCodigo(codigoLimpo);
        if (encontrado.isPresent()) {
            EventoDTO evento = encontrado.get();
            model.addAttribute("evento", evento);
            if (evento.ativo()) {
                model.addAttribute("ok", true);
            } else {
                model.addAttribute("ok", false);
                model.addAttribute("inativo", true);
            }
        } else {
            model.addAttribute("ok", false);
            model.addAttribute("codigoInformado", codigoLimpo);
        }
        return "verificar";
    }

    private String sanitizarCodigo(String codigo) {
        if (codigo == null) return "";
        String limpo = codigo.trim();
        if (limpo.length() > CODIGO_MAX_LENGTH) {
            limpo = limpo.substring(0, CODIGO_MAX_LENGTH);
        }
        if (!limpo.matches("[a-zA-Z0-9\\-]+")) return "";
        return limpo;
    }
}
