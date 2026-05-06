package com.arenapernambuco.repository;

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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
            log.warn("Firebase: nó 'eventos' vazio ou não encontrado");
            return Collections.emptyList();
        }
        log.info("Firebase: {} nós encontrados em 'eventos'", snapshot.getChildrenCount());
        List<Evento> lista = new ArrayList<>();
        for (DataSnapshot filho : snapshot.getChildren()) {
            try {
                lista.add(snapshotParaEvento(filho));
            } catch (Exception e) {
                log.warn("Erro ao converter evento id={}: {} — dados brutos: {}",
                        filho.getKey(), e.getMessage(), filho.getValue());
            }
        }
        log.info("Firebase: {} evento(s) convertidos com sucesso", lista.size());
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
        String codigoNormalizado = codigo.trim().toUpperCase();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<DataSnapshot> resultado = new AtomicReference<>();

        eventosRef.orderByChild("codigoVerificacao").equalTo(codigoNormalizado)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        resultado.set(snapshot);
                        latch.countDown();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        log.error("Busca por codigo cancelada: {}", error.getMessage());
                        latch.countDown();
                    }
                });

        try {
            if (!latch.await(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)) {
                log.warn("Timeout ao buscar por codigo '{}'", codigoNormalizado);
                return Optional.empty();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }

        DataSnapshot snapshot = resultado.get();
        if (snapshot == null || !snapshot.exists()) {
            return Optional.empty();
        }

        for (DataSnapshot filho : snapshot.getChildren()) {
            try {
                return Optional.of(snapshotParaEvento(filho));
            } catch (Exception e) {
                log.warn("Erro ao converter evento da query por codigo: {}", e.getMessage());
            }
        }
        return Optional.empty();
    }

    @Override
    public Evento salvar(Evento evento) {
        Map<String, Object> dados = eventoParaMapa(evento);
        aguardarEscrita(eventosRef.child(evento.id()).setValueAsync(dados), "salvar", evento.id());
        return evento;
    }

    @Override
    public Evento atualizar(String id, Evento evento) {
        Map<String, Object> dados = eventoParaMapa(evento);
        aguardarEscrita(eventosRef.child(id).setValueAsync(dados), "atualizar", id);
        return evento;
    }

    @Override
    public void remover(String id) {
        aguardarEscrita(eventosRef.child(id).removeValueAsync(), "remover", id);
    }

    private void aguardarEscrita(com.google.api.core.ApiFuture<Void> future, String operacao, String id) {
        try {
            future.get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
        } catch (java.util.concurrent.ExecutionException e) {
            throw new RuntimeException("Falha ao " + operacao + " evento id=" + id, e.getCause());
        } catch (java.util.concurrent.TimeoutException e) {
            throw new RuntimeException("Timeout ao " + operacao + " evento id=" + id, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operação interrompida ao " + operacao + " evento id=" + id, e);
        }
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

    private static final DateTimeFormatter SERIALIZAR_FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static final List<DateTimeFormatter> DATA_HORA_FORMATOS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    );

    @SuppressWarnings("unchecked")
    private Evento snapshotParaEvento(DataSnapshot snapshot) {
        String id = snapshot.getKey();
        Map<String, Object> dados;

        Object valor = snapshot.getValue();
        if (valor instanceof Map) {
            dados = (Map<String, Object>) valor;
        } else {
            throw new IllegalStateException("Formato inválido para evento id=" + id + " valor=" + valor);
        }

        log.debug("Firebase evento id={} campos={}", id, dados.keySet());

        String titulo = Objects.toString(dados.get("titulo"), "Sem título");
        String dataHoraStr = Objects.toString(dados.get("dataHora"), null);
        LocalDateTime dataHora = parsarDataHora(id, dataHoraStr);
        String categoria = Objects.toString(dados.get("categoria"), "Geral");
        String codigoVerificacao = Objects.toString(dados.get("codigoVerificacao"), id);
        String descricaoCurta = Objects.toString(dados.get("descricaoCurta"), "");
        String descricaoCompleta = Objects.toString(dados.get("descricaoCompleta"), "");
        String imagemUrl = Objects.toString(dados.get("imagemUrl"), "");
        boolean ativo = parsarBoolean(dados.get("ativo"), true);

        int capacidade = parsarInt(dados.get("capacidade"), 0);
        int inscritos   = parsarInt(dados.get("inscritos"), 0);

        return new Evento(id, titulo, dataHora, categoria, codigoVerificacao,
                descricaoCurta, descricaoCompleta, imagemUrl, ativo, capacidade, inscritos);
    }

    private LocalDateTime parsarDataHora(String id, String valor) {
        if (valor == null || valor.isBlank()) {
            log.warn("Evento id={} sem campo 'dataHora', usando agora como fallback", id);
            return LocalDateTime.now();
        }
        for (DateTimeFormatter fmt : DATA_HORA_FORMATOS) {
            try {
                return LocalDateTime.parse(valor, fmt);
            } catch (DateTimeParseException ignored) {
            }
        }
        log.warn("Evento id={} — não foi possível interpretar dataHora='{}', usando agora", id, valor);
        return LocalDateTime.now();
    }

    private boolean parsarBoolean(Object valor, boolean defaultValue) {
        if (valor == null) return defaultValue;
        if (valor instanceof Boolean b) return b;
        if (valor instanceof Number n) return n.intValue() != 0;
        return Boolean.parseBoolean(valor.toString());
    }

    private int parsarInt(Object valor, int defaultValue) {
        if (valor == null) return defaultValue;
        if (valor instanceof Number n) {
            long lv = n.longValue();
            if (lv < 0 || lv > Integer.MAX_VALUE) {
                log.warn("Valor numérico fora do intervalo int: {}, usando default", lv);
                return defaultValue;
            }
            return (int) lv;
        }
        try { return Integer.parseInt(valor.toString()); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    private Map<String, Object> eventoParaMapa(Evento evento) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("titulo", evento.titulo());
        mapa.put("dataHora", evento.dataHora().format(SERIALIZAR_FORMATO));
        mapa.put("categoria", evento.categoria());
        mapa.put("codigoVerificacao", evento.codigoVerificacao());
        mapa.put("descricaoCurta", evento.descricaoCurta());
        mapa.put("descricaoCompleta", evento.descricaoCompleta());
        mapa.put("imagemUrl", evento.imagemUrl());
        mapa.put("ativo", evento.ativo());
        mapa.put("capacidade", evento.capacidade());
        mapa.put("inscritos", evento.inscritos());
        return mapa;
    }
}
