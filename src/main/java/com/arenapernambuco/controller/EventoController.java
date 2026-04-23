package com.arenapernambuco.controller;

import com.arenapernambuco.dto.EventoDTO;
import com.arenapernambuco.dto.EventoFiltroDTO;
import com.arenapernambuco.service.EventoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;

@Controller
public class EventoController {

    private static final Set<String> CATEGORIAS_VALIDAS =
            Set.of("Futebol", "Música", "Corporativo", "Cultural", "Teatro");

    private static final Set<String> ORDENS_VALIDAS = Set.of("proximos", "recentes");

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping("/eventos")
    public String listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String data,
            @RequestParam(required = false) String ordem,
            Model model) {

        String categoriaSanitizada = (categoria != null && CATEGORIAS_VALIDAS.contains(categoria)) ? categoria : null;
        String ordemSanitizada = (ordem != null && ORDENS_VALIDAS.contains(ordem)) ? ordem : "proximos";
        LocalDate dataParsed = parseData(data);

        EventoFiltroDTO filtro = new EventoFiltroDTO(categoriaSanitizada, dataParsed, ordemSanitizada);
        List<EventoDTO> eventos = eventoService.filtrar(filtro);

        model.addAttribute("eventos", eventos);
        model.addAttribute("categoriaAtiva", categoriaSanitizada);
        model.addAttribute("dataAtiva", dataParsed != null ? dataParsed.toString() : null);
        model.addAttribute("ordemAtiva", ordemSanitizada);
        model.addAttribute("categorias", CATEGORIAS_VALIDAS);
        return "eventos";
    }

    @GetMapping("/eventos/{id}")
    public String detalhe(@PathVariable String id, Model model) {
        EventoDTO evento = eventoService.buscarDetalhePorId(id);
        model.addAttribute("evento", evento);
        return "evento-detalhe";
    }

    private LocalDate parseData(String data) {
        if (data == null || data.isBlank()) return null;
        try {
            return LocalDate.parse(data.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
