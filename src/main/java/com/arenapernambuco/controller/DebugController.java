package com.arenapernambuco.controller;

import com.arenapernambuco.dto.EventoDTO;
import com.arenapernambuco.dto.EventoFiltroDTO;
import com.arenapernambuco.model.Evento;
import com.arenapernambuco.repository.EventoRepository;
import com.arenapernambuco.service.EventoService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@Profile("debug")
public class DebugController {

    private final EventoRepository repository;
    private final EventoService eventoService;

    public DebugController(EventoRepository repository, EventoService eventoService) {
        this.repository = repository;
        this.eventoService = eventoService;
    }

    @GetMapping("/debug/firebase")
    public Map<String, Object> debugFirebase() {
        Map<String, Object> resultado = new LinkedHashMap<>();

        List<Evento> todos = repository.buscarTodos();
        resultado.put("repo_buscarTodos_count", todos.size());
        resultado.put("repo_buscarAtivos_count", repository.buscarAtivos().size());

        EventoFiltroDTO semFiltro = new EventoFiltroDTO(null, null, "proximos");

        List<EventoDTO> dtos;
        String erroFiltrar = null;
        try {
            dtos = eventoService.filtrar(semFiltro);
        } catch (Exception ex) {
            dtos = List.of();
            erroFiltrar = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        }
        resultado.put("service_filtrar_count", dtos.size());
        resultado.put("service_filtrar_erro", erroFiltrar);

        if (!dtos.isEmpty()) {
            resultado.put("primeiro_dto", dtos.get(0));
        }

        Map<String, Object> detalheEventos = new LinkedHashMap<>();
        for (Evento e : todos) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("ativo", e.ativo());
            info.put("dataHora", e.dataHora() != null ? e.dataHora().toString() : "NULL");
            info.put("categoria", e.categoria());
            String toDto = null;
            try {
                eventoService.buscarDetalhePorId(e.id());
                toDto = "OK";
            } catch (Exception ex) {
                toDto = "ERRO: " + ex.getClass().getSimpleName() + " — " + ex.getMessage();
            }
            info.put("toDTO", toDto);
            detalheEventos.put(e.id(), info);
        }
        resultado.put("detalhe_por_evento", detalheEventos);

        return resultado;
    }
}
