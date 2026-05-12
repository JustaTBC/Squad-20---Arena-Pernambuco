package com.arenapernambuco.service;

import com.arenapernambuco.dto.SugestaoDTO;
import com.arenapernambuco.dto.SugestaoFormDTO;
import com.arenapernambuco.model.StatusSugestao;
import com.arenapernambuco.model.Sugestao;
import com.arenapernambuco.repository.SugestaoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SugestaoService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH'h'mm", Locale.forLanguageTag("pt-BR"));
    private static final Pattern ID_VALIDO = Pattern.compile("^[a-zA-Z0-9\\-]+$");

    private final SugestaoRepository repository;

    public SugestaoService(SugestaoRepository repository) {
        this.repository = repository;
    }

    public SugestaoDTO registrar(SugestaoFormDTO form) {
        Sugestao sugestao = new Sugestao(
                UUID.randomUUID().toString(),
                form.getTipoEvento().trim(),
                form.getDescricaoIdeia().trim(),
                form.getPublicoEstimado(),
                StatusSugestao.PENDENTE,
                LocalDateTime.now()
        );
        return toDTO(repository.salvar(sugestao));
    }

    public List<SugestaoDTO> listarTodas() {
        return repository.buscarTodas().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void remover(String id) {
        if (!ID_VALIDO.matcher(id).matches()) {
            throw new IllegalArgumentException("ID de sugestao invalido: " + id);
        }
        repository.remover(id);
    }

    private SugestaoDTO toDTO(Sugestao s) {
        return new SugestaoDTO(
                s.id(),
                s.tipoEvento(),
                s.descricaoIdeia(),
                s.publicoEstimado(),
                s.status(),
                s.criadaEm() != null ? s.criadaEm().format(FORMATTER) : "Data não informada"
        );
    }
}
