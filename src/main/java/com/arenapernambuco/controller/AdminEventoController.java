package com.arenapernambuco.controller;

import com.arenapernambuco.dto.EventoFormDTO;
import com.arenapernambuco.service.EventoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/eventos")
public class AdminEventoController {

    private final EventoService eventoService;

    public AdminEventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("eventos", eventoService.listarTodos());
        return "admin/eventos-lista";
    }

    @GetMapping("/novo")
    public String formNovo(Model model) {
        model.addAttribute("form", new EventoFormDTO());
        model.addAttribute("categorias", EventoService.CATEGORIAS_VALIDAS);
        return "admin/evento-form";
    }

    @PostMapping("/novo")
    public String cadastrar(@Valid @ModelAttribute("form") EventoFormDTO form,
                            BindingResult result,
                            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", EventoService.CATEGORIAS_VALIDAS);
            return "admin/evento-form";
        }
        eventoService.cadastrar(form);
        return "redirect:/admin/eventos";
    }

    @GetMapping("/{id}/editar")
    public String formEditar(@PathVariable String id, Model model) {
        model.addAttribute("form", eventoService.toFormDTO(id));
        model.addAttribute("eventoId", id);
        model.addAttribute("categorias", EventoService.CATEGORIAS_VALIDAS);
        return "admin/evento-form";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable String id,
                         @Valid @ModelAttribute("form") EventoFormDTO form,
                         BindingResult result,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("eventoId", id);
            model.addAttribute("categorias", EventoService.CATEGORIAS_VALIDAS);
            return "admin/evento-form";
        }
        eventoService.atualizar(id, form);
        return "redirect:/admin/eventos";
    }

    @PostMapping("/{id}/remover")
    public String remover(@PathVariable String id) {
        eventoService.remover(id);
        return "redirect:/admin/eventos";
    }
}
