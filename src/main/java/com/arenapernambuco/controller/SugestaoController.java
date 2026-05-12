package com.arenapernambuco.controller;

import com.arenapernambuco.dto.SugestaoFormDTO;
import com.arenapernambuco.service.SugestaoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sugestoes")
public class SugestaoController {

    private final SugestaoService sugestaoService;

    public SugestaoController(SugestaoService sugestaoService) {
        this.sugestaoService = sugestaoService;
    }

    @GetMapping
    public String formulario(Model model) {
        model.addAttribute("form", new SugestaoFormDTO());
        return "sugestao-form";
    }

    @PostMapping
    public String enviar(@Valid @ModelAttribute("form") SugestaoFormDTO form,
                         BindingResult result) {
        if (result.hasErrors()) {
            return "sugestao-form";
        }
        sugestaoService.registrar(form);
        return "redirect:/sugestoes/confirmacao";
    }

    @GetMapping("/confirmacao")
    public String confirmacao() {
        return "sugestao-confirmacao";
    }
}
