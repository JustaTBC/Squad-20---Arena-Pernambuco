package com.arenapernambuco.repository;

import com.arenapernambuco.dto.EventoFiltroDTO;
import com.arenapernambuco.model.Evento;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Repository
@Primary
@Profile("firebase")
public class EventoFirebaseRepository implements EventoRepository {

    private static final Logger log = LoggerFactory.getLogger(EventoFirebaseRepository.class);
    private static final long TIMEOUT_SEGUNDOS = 10;

    private final DatabaseReference eventosRef;

    public EventoFirebaseRepository(DatabaseReference eventosRef) {
        this.eventosRef = eventosRef;
    }

    @Override
    public List<Evento> buscarTodos() {
        DataSnapshot snapshot = lerSnapshot(eventosRef);
        if (snapshot == null || !snapshot.exists()) {
            return Collections.emptyList();
        }
        List<Evento> lista = new ArrayList<>();
        for (DataSnapshot filho : snapshot.getChildren()) {
            try {
                lista.add(snapshotParaEvento(filho));
            } catch (Exception e) {
                log.warn("Erro ao converter evento id={}: {}", filho.getKey(), e.getMessage());
            }
        }
        return lista;
    }

    @Override
    public List<Evento> buscarAtivos() {
        return buscarTodos().stream()
                .filter(Evento::ativo)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Evento> buscarPorId(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        DataSnapshot snapshot = lerSnapshot(eventosRef.child(id));
        if (snapshot == null || !snapshot.exists()) {
            return Optional.empty();
        }
        try {
            return Optional.of(snapshotParaEvento(snapshot));
        } catch (Exception e) {
            log.warn("Erro ao converter evento id={}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<Evento> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) return Optional.empty();
        return buscarTodos().stream()
                .filter(e -> e.codigoVerificacao().equalsIgnoreCase(codigo.trim()))
                .findFirst();
    }

    @Override
    public List<Evento> filtrar(EventoFiltroDTO filtro) {
        var stream = buscarTodos().stream().filter(Evento::ativo);

        if (filtro.categoria() != null && !filtro.categoria().isBlank()) {
            stream = stream.filter(e -> e.categoria().equalsIgnoreCase(filtro.categoria()));
        }

        if (filtro.data() != null) {
            stream = stream.filter(e -> e.dataHora().toLocalDate().equals(filtro.data()));
        }

        Comparator<Evento> comparator = Comparator.comparing(Evento::dataHora);
        if ("recentes".equalsIgnoreCase(filtro.ordem())) {
            comparator = comparator.reversed();
        }

        return stream.sorted(comparator).collect(Collectors.toList());
    }

    public void salvar(Evento evento) {
        Map<String, Object> dados = eventoParaMapa(evento);
        eventosRef.child(evento.id()).setValueAsync(dados);
    }

    // -------------------------------------------------------------------------

    private DataSnapshot lerSnapshot(DatabaseReference ref) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<DataSnapshot> resultado = new AtomicReference<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                resultado.set(snapshot);
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                log.error("Leitura cancelada pelo Firebase: {}", error.getMessage());
                latch.countDown();
            }
        });

        try {
            boolean concluido = latch.await(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
            if (!concluido) {
                log.warn("Timeout ao aguardar resposta do Firebase ({}s)", TIMEOUT_SEGUNDOS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Thread interrompida ao aguardar Firebase");
        }

        return resultado.get();
    }

    @SuppressWarnings("unchecked")
    private Evento snapshotParaEvento(DataSnapshot snapshot) {
        String id = snapshot.getKey();
        Map<String, Object> dados;

        Object valor = snapshot.getValue();
        if (valor instanceof Map) {
            dados = (Map<String, Object>) valor;
        } else {
            throw new IllegalStateException("Formato inválido para evento id=" + id);
        }

        String titulo = (String) dados.get("titulo");
        LocalDateTime dataHora = LocalDateTime.parse((String) dados.get("dataHora"));
        String categoria = (String) dados.get("categoria");
        String codigoVerificacao = (String) dados.get("codigoVerificacao");
        String descricaoCurta = (String) dados.get("descricaoCurta");
        String descricaoCompleta = (String) dados.get("descricaoCompleta");
        String imagemUrl = (String) dados.get("imagemUrl");
        boolean ativo = Boolean.TRUE.equals(dados.get("ativo"));

        return new Evento(id, titulo, dataHora, categoria, codigoVerificacao,
                descricaoCurta, descricaoCompleta, imagemUrl, ativo);
    }

    private Map<String, Object> eventoParaMapa(Evento evento) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("titulo", evento.titulo());
        mapa.put("dataHora", evento.dataHora().toString());
        mapa.put("categoria", evento.categoria());
        mapa.put("codigoVerificacao", evento.codigoVerificacao());
        mapa.put("descricaoCurta", evento.descricaoCurta());
        mapa.put("descricaoCompleta", evento.descricaoCompleta());
        mapa.put("imagemUrl", evento.imagemUrl());
        mapa.put("ativo", evento.ativo());
        return mapa;
    }
}
