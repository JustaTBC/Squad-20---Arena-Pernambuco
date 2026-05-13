package com.arenapernambuco.service;

import com.arenapernambuco.dto.EventoDTO;
import com.arenapernambuco.dto.IngressoDTO;
import com.arenapernambuco.model.Ingresso;
import com.arenapernambuco.repository.IngressoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class IngressoService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH'h'mm", Locale.forLanguageTag("pt-BR"));

    private static final Map<String, String> CORES_CATEGORIA = Map.of(
            "futebol", "#22c55e",
            "música", "#FF6B35",
            "corporativo", "#3b82f6",
            "cultural", "#7C3AED",
            "teatro", "#ec4899"
    );

    private final IngressoRepository ingressoRepository;
    private final EventoService eventoService;

    public IngressoService(IngressoRepository ingressoRepository, EventoService eventoService) {
        this.ingressoRepository = ingressoRepository;
        this.eventoService = eventoService;
    }

    public IngressoDTO comprar(String eventoId, int quantidade, String username) {
        if (quantidade < 1 || quantidade > 5) {
            throw new IllegalArgumentException("Quantidade deve ser entre 1 e 5");
        }
        EventoDTO evento = eventoService.buscarDetalhePorId(eventoId);
        int vagas = evento.capacidade() - evento.inscritos();
        if (vagas < quantidade) {
            throw new IllegalStateException(
                    "Vagas insuficientes. Disponível: " + vagas);
        }
        String id = UUID.randomUUID().toString();
        String codigo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Ingresso ingresso = new Ingresso(id, eventoId, username, codigo, quantidade,
                LocalDateTime.now(), false, null);
        ingressoRepository.salvar(ingresso);
        eventoService.incrementarInscritos(eventoId, quantidade);
        return toDTO(ingresso, evento);
    }

    public void cancelar(String ingressoId, String username) {
        Ingresso ingresso = ingressoRepository.buscarPorId(username, ingressoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ingresso não encontrado: " + ingressoId));
        if (ingresso.cancelado()) {
            throw new IllegalStateException("Ingresso já foi cancelado");
        }
        ingressoRepository.cancelar(username, ingressoId);
        eventoService.decrementarInscritos(ingresso.eventoId(), ingresso.quantidade());
    }

    public Optional<IngressoDTO> buscarPorCodigo(String codigoIngresso) {
        return ingressoRepository.buscarPorCodigo(codigoIngresso)
                .map(ingresso -> {
                    EventoDTO evento;
                    try {
                        evento = eventoService.buscarDetalhePorId(ingresso.eventoId());
                    } catch (Exception e) {
                        evento = new EventoDTO(ingresso.eventoId(), "Evento não disponível",
                                "-", "-", "", "", "", "#6b7280", false, 0, 0, "N/D");
                    }
                    return toDTO(ingresso, evento);
                });
    }

    public List<IngressoDTO> listarPorUsername(String username) {
        return ingressoRepository.buscarPorUsername(username).stream()
                .map(ingresso -> {
                    EventoDTO evento;
                    try {
                        evento = eventoService.buscarDetalhePorId(ingresso.eventoId());
                    } catch (Exception e) {
                        evento = new EventoDTO(ingresso.eventoId(), "Evento não disponível",
                                "-", "-", "", "", "", "#6b7280", false, 0, 0, "N/D");
                    }
                    return toDTO(ingresso, evento);
                })
                .collect(Collectors.toList());
    }

    private IngressoDTO toDTO(Ingresso ingresso, EventoDTO evento) {
        String badgeCor = CORES_CATEGORIA.getOrDefault(
                evento.categoria().toLowerCase(), "#6b7280");
        String dataCancelamentoFormatada = ingresso.dataCancelamento() != null
                ? ingresso.dataCancelamento().format(FORMATTER) : null;
        return new IngressoDTO(
                ingresso.id(),
                ingresso.eventoId(),
                evento.titulo(),
                evento.dataFormatada(),
                evento.categoria(),
                evento.imagemUrl(),
                badgeCor,
                evento.codigoVerificacao(),
                ingresso.codigoIngresso(),
                ingresso.quantidade(),
                ingresso.dataCompra().format(FORMATTER),
                ingresso.cancelado(),
                dataCancelamentoFormatada);
    }
}
