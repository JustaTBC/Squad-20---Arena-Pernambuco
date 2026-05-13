package com.arenapernambuco.service;

import com.arenapernambuco.dto.EventoDTO;
import com.arenapernambuco.dto.EventoFiltroDTO;
import com.arenapernambuco.dto.EventoFormDTO;
import com.arenapernambuco.exception.EventoNaoEncontradoException;
import com.arenapernambuco.model.Evento;
import com.arenapernambuco.repository.EventoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EventoService {

    public static final Set<String> CATEGORIAS_VALIDAS = new LinkedHashSet<>(List.of(
            "Futebol", "Música", "Corporativo", "Cultural", "Teatro"));

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH'h'mm", Locale.forLanguageTag("pt-BR"));

    private static final DateTimeFormatter FORM_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

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

    public List<EventoDTO> listarTodos() {
        return repository.buscarTodos().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<EventoDTO> filtrar(EventoFiltroDTO filtro) {
        Stream<Evento> stream = repository.buscarAtivos().stream();

        if (filtro.categoria() != null && !filtro.categoria().isBlank()) {
            stream = stream.filter(e -> e.categoria().equalsIgnoreCase(filtro.categoria()));
        }

        if (filtro.data() != null) {
            stream = stream.filter(e -> e.dataHora() != null
                    && e.dataHora().toLocalDate().equals(filtro.data()));
        }

        Comparator<Evento> comparator = Comparator.comparing(
                Evento::dataHora, Comparator.nullsLast(Comparator.naturalOrder()));
        if ("recentes".equalsIgnoreCase(filtro.ordem())) {
            comparator = comparator.reversed();
        }

        return stream.sorted(comparator)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void validarId(String id) {
        if (id == null || !id.matches("[a-zA-Z0-9\\-]+")) {
            throw new EventoNaoEncontradoException(id);
        }
    }

    public EventoDTO buscarDetalhePorId(String id) {
        validarId(id);
        Evento evento = repository.buscarPorId(id)
                .orElseThrow(() -> new EventoNaoEncontradoException(id));
        return toDTO(evento);
    }

    public Optional<EventoDTO> verificarPorCodigo(String codigo) {
        return repository.buscarPorCodigo(codigo).map(this::toDTO);
    }

    public EventoDTO cadastrar(EventoFormDTO form) {
        String id = UUID.randomUUID().toString();
        String codigo = resolverCodigo(form.getCodigoVerificacao(), id);
        LocalDateTime dataHora = parsarDataHoraForm(form.getDataHora());

        Evento evento = new Evento(
                id,
                form.getTitulo(),
                dataHora,
                normalizarCategoria(form.getCategoria()),
                codigo,
                nvl(form.getDescricaoCurta()),
                nvl(form.getDescricaoCompleta()),
                nvl(form.getImagemUrl()),
                form.isAtivo(),
                form.getCapacidade(),
                form.getInscritos()
        );
        return toDTO(repository.salvar(evento));
    }

    public EventoDTO atualizar(String id, EventoFormDTO form) {
        validarId(id);
        repository.buscarPorId(id)
                .orElseThrow(() -> new EventoNaoEncontradoException(id));

        String codigo = resolverCodigo(form.getCodigoVerificacao(), id);
        LocalDateTime dataHora = parsarDataHoraForm(form.getDataHora());

        Evento atualizado = new Evento(
                id,
                form.getTitulo(),
                dataHora,
                normalizarCategoria(form.getCategoria()),
                codigo,
                nvl(form.getDescricaoCurta()),
                nvl(form.getDescricaoCompleta()),
                nvl(form.getImagemUrl()),
                form.isAtivo(),
                form.getCapacidade(),
                form.getInscritos()
        );
        return toDTO(repository.atualizar(id, atualizado));
    }

    public void remover(String id) {
        validarId(id);
        repository.buscarPorId(id)
                .orElseThrow(() -> new EventoNaoEncontradoException(id));
        repository.remover(id);
    }

    public void incrementarInscritos(String eventoId, int quantidade) {
        validarId(eventoId);
        Evento evento = repository.buscarPorId(eventoId)
                .orElseThrow(() -> new EventoNaoEncontradoException(eventoId));
        Evento atualizado = new Evento(
                evento.id(), evento.titulo(), evento.dataHora(), evento.categoria(),
                evento.codigoVerificacao(), evento.descricaoCurta(), evento.descricaoCompleta(),
                evento.imagemUrl(), evento.ativo(), evento.capacidade(),
                evento.inscritos() + quantidade);
        repository.atualizar(eventoId, atualizado);
    }

    public void decrementarInscritos(String eventoId, int quantidade) {
        validarId(eventoId);
        Evento evento = repository.buscarPorId(eventoId)
                .orElseThrow(() -> new EventoNaoEncontradoException(eventoId));
        int novoInscritos = Math.max(0, evento.inscritos() - quantidade);
        Evento atualizado = new Evento(
                evento.id(), evento.titulo(), evento.dataHora(), evento.categoria(),
                evento.codigoVerificacao(), evento.descricaoCurta(), evento.descricaoCompleta(),
                evento.imagemUrl(), evento.ativo(), evento.capacidade(), novoInscritos);
        repository.atualizar(eventoId, atualizado);
    }

    public EventoFormDTO toFormDTO(String id) {
        validarId(id);
        Evento e = repository.buscarPorId(id)
                .orElseThrow(() -> new EventoNaoEncontradoException(id));

        EventoFormDTO form = new EventoFormDTO();
        form.setTitulo(e.titulo());
        form.setCategoria(e.categoria());
        form.setDataHora(e.dataHora() != null ? e.dataHora().format(FORM_FORMATTER) : "");
        form.setDescricaoCurta(e.descricaoCurta());
        form.setDescricaoCompleta(e.descricaoCompleta());
        form.setImagemUrl(e.imagemUrl());
        form.setCodigoVerificacao(e.codigoVerificacao());
        form.setAtivo(e.ativo());
        form.setCapacidade(e.capacidade());
        form.setInscritos(e.inscritos());
        return form;
    }

    private String normalizarCategoria(String categoria) {
        if (categoria == null) return "";
        return CATEGORIAS_VALIDAS.stream()
                .filter(c -> c.equalsIgnoreCase(categoria))
                .findFirst()
                .orElse(categoria);
    }

    private String resolverCodigo(String codigoInformado, String id) {
        if (codigoInformado != null && !codigoInformado.isBlank()) {
            return codigoInformado.trim().toUpperCase();
        }
        String stripped = id.replace("-", "");
        return (stripped.length() >= 6 ? stripped.substring(0, 6) : stripped).toUpperCase();
    }

    private LocalDateTime parsarDataHoraForm(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Data e horário são obrigatórios");
        }
        try {
            return LocalDateTime.parse(valor, FORM_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de data inválido: " + valor);
        }
    }

    private String nvl(String valor) {
        return valor != null ? valor : "";
    }

    private EventoDTO toDTO(Evento e) {
        String dataFormatada = e.dataHora() != null
                ? e.dataHora().format(FORMATTER)
                : "Data não informada";
        return new EventoDTO(
                e.id(),
                e.titulo(),
                dataFormatada,
                e.categoria(),
                e.descricaoCurta(),
                e.descricaoCompleta(),
                e.imagemUrl(),
                CORES_CATEGORIA.getOrDefault(e.categoria().toLowerCase(), "#6b7280"),
                e.ativo(),
                e.capacidade(),
                e.inscritos(),
                e.codigoVerificacao()
        );
    }
}
