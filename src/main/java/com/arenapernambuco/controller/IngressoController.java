package com.arenapernambuco.controller;

import com.arenapernambuco.dto.IngressoFormDTO;
import com.arenapernambuco.exception.EventoNaoEncontradoException;
import com.arenapernambuco.service.IngressoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class IngressoController {

    private final IngressoService ingressoService;

    public IngressoController(IngressoService ingressoService) {
        this.ingressoService = ingressoService;
    }

    @GetMapping("/participante/ingressos")
    public String listar(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        model.addAttribute("ingressos", ingressoService.listarPorUsername(principal.getName()));
        return "participante/ingressos";
    }

    @PostMapping("/ingressos/comprar")
    public String comprar(
            @Valid @ModelAttribute IngressoFormDTO form,
            BindingResult binding,
            Principal principal,
            RedirectAttributes redirect) {

        if (binding.hasErrors()) {
            redirect.addFlashAttribute("erro", "Quantidade inválida. Escolha entre 1 e 5 ingressos.");
            String eventoId = form.getEventoId();
            if (eventoId != null && eventoId.matches("[a-zA-Z0-9\\-]+")) {
                return "redirect:/eventos/" + eventoId;
            }
            return "redirect:/eventos";
        }

        try {
            ingressoService.comprar(form.getEventoId(), form.getQuantidade(), principal.getName());
            redirect.addFlashAttribute("sucesso", "Ingresso comprado com sucesso!");
            return "redirect:/participante/ingressos";
        } catch (EventoNaoEncontradoException e) {
            redirect.addFlashAttribute("erro", "Evento não encontrado.");
            return "redirect:/eventos";
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/eventos/" + form.getEventoId();
        }
    }

    @PostMapping("/ingressos/{id}/cancelar")
    public String cancelar(
            @PathVariable String id,
            Principal principal,
            RedirectAttributes redirect) {

        try {
            ingressoService.cancelar(id, principal.getName());
            redirect.addFlashAttribute("sucesso", "Ingresso cancelado com sucesso.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/participante/ingressos";
    }
}
