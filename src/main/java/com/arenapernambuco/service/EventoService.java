package com.arenapernambuco.service;

import com.arenapernambuco.dto.EventoDTO;
import com.arenapernambuco.dto.EventoFiltroDTO;
import com.arenapernambuco.exception.EventoNaoEncontradoException;
import com.arenapernambuco.model.Evento;
import com.arenapernambuco.repository.EventoRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventoService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH'h'mm", new Locale("pt", "BR"));

    private static final Map<String, String> CORES_CATEGORIA = Map.of(
            "futebol", "#22c55e",
            "música", "#FF6B35",
            "corporativo", "#3b82f6",
            "cultural", "#7C3AED",
            "teatro", "#ec4899"
    );

    private final EventoRepository repository;

    public EventoService(EventoRepository repository) {
        this.repository = repository;
    }

    public List<EventoDTO> listarAtivos() {
        return repository.buscarAtivos().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<EventoDTO> filtrar(EventoFiltroDTO filtro) {
        return repository.filtrar(filtro).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public EventoDTO buscarDetalhePorId(String id) {
        Evento evento = repository.buscarPorId(id)
                .orElseThrow(() -> new EventoNaoEncontradoException(id));
        return toDTO(evento);
    }

    public Optional<EventoDTO> verificarPorCodigo(String codigo) {
        return repository.buscarPorCodigo(codigo).map(this::toDTO);
    }

    private EventoDTO toDTO(Evento e) {
        return new EventoDTO(
                e.id(),
                e.titulo(),
                e.dataHora().format(FORMATTER),
                e.categoria(),
                e.descricaoCurta(),
                e.descricaoCompleta(),
                e.imagemUrl(),
                CORES_CATEGORIA.getOrDefault(e.categoria().toLowerCase(), "#6b7280")
        );
    }
}
