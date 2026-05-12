package com.arenapernambuco.controller;

import com.arenapernambuco.service.SugestaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/sugestoes")
public class AdminSugestaoController {

    private final SugestaoService sugestaoService;

    public AdminSugestaoController(SugestaoService sugestaoService) {
        this.sugestaoService = sugestaoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("sugestoes", sugestaoService.listarTodas());
        return "admin/sugestoes-lista";
    }

    @PostMapping("/{id}/remover")
    public String remover(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            sugestaoService.remover(id);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", "Sugestão não encontrada.");
        }
        return "redirect:/admin/sugestoes";
    }
}
