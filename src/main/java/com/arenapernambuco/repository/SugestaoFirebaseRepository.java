package com.arenapernambuco.repository;

import com.arenapernambuco.model.StatusSugestao;
import com.arenapernambuco.model.Sugestao;
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

@Repository
@Primary
@Profile("firebase")
public class SugestaoFirebaseRepository implements SugestaoRepository {

    private static final Logger log = LoggerFactory.getLogger(SugestaoFirebaseRepository.class);
    private static final long TIMEOUT_SEGUNDOS = 10;
    private static final DateTimeFormatter SERIALIZAR_FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final DatabaseReference sugestoesRef;

    public SugestaoFirebaseRepository(DatabaseReference sugestoesRef) {
        this.sugestoesRef = sugestoesRef;
    }

    @Override
    public Sugestao salvar(Sugestao sugestao) {
        Map<String, Object> dados = sugestaoParaMapa(sugestao);
        aguardarEscrita(sugestoesRef.child(sugestao.id()).setValueAsync(dados), "salvar", sugestao.id());
        return sugestao;
    }

    @Override
    public void remover(String id) {
        aguardarEscrita(sugestoesRef.child(id).removeValueAsync(), "remover", id);
    }

    @Override
    public List<Sugestao> buscarTodas() {
        DataSnapshot snapshot = lerSnapshot(sugestoesRef);
        if (snapshot == null || !snapshot.exists()) {
            log.warn("Firebase: nó 'sugestoes' vazio ou não encontrado");
            return Collections.emptyList();
        }
        log.info("Firebase: {} nós encontrados em 'sugestoes'", snapshot.getChildrenCount());
        List<Sugestao> lista = new ArrayList<>();
        for (DataSnapshot filho : snapshot.getChildren()) {
            try {
                lista.add(snapshotParaSugestao(filho));
            } catch (Exception e) {
                log.warn("Erro ao converter sugestão id={}: {} — dados brutos: {}",
                        filho.getKey(), e.getMessage(), filho.getValue());
            }
        }
        log.info("Firebase: {} sugestão(ões) convertida(s) com sucesso", lista.size());
        return lista;
    }

    @SuppressWarnings("unchecked")
    private Sugestao snapshotParaSugestao(DataSnapshot snapshot) {
        String id = snapshot.getKey();
        Object valor = snapshot.getValue();
        if (!(valor instanceof Map)) {
            throw new IllegalStateException("Formato inválido para sugestão id=" + id + " valor=" + valor);
        }
        Map<String, Object> dados = (Map<String, Object>) valor;

        String tipoEvento   = Objects.toString(dados.get("tipoEvento"), "");
        String descricao    = Objects.toString(dados.get("descricaoIdeia"), "");
        int publico         = parsarInt(dados.get("publicoEstimado"), 0);
        String statusStr    = Objects.toString(dados.get("status"), "PENDENTE");
        StatusSugestao status;
        try {
            status = StatusSugestao.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            log.warn("Status desconhecido '{}' para sugestão id={}, usando PENDENTE", statusStr, id);
            status = StatusSugestao.PENDENTE;
        }
        String criadaEmStr  = Objects.toString(dados.get("criadaEm"), null);
        LocalDateTime criadaEm;
        try {
            criadaEm = LocalDateTime.parse(criadaEmStr, SERIALIZAR_FORMATO);
        } catch (DateTimeParseException | NullPointerException e) {
            log.warn("Sugestão id={} — não foi possível interpretar criadaEm='{}', usando agora", id, criadaEmStr);
            criadaEm = LocalDateTime.now();
        }

        return new Sugestao(id, tipoEvento, descricao, publico, status, criadaEm);
    }

    private Map<String, Object> sugestaoParaMapa(Sugestao s) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("tipoEvento", s.tipoEvento());
        mapa.put("descricaoIdeia", s.descricaoIdeia());
        mapa.put("publicoEstimado", s.publicoEstimado());
        mapa.put("status", s.status().name());
        mapa.put("criadaEm", s.criadaEm().format(SERIALIZAR_FORMATO));
        return mapa;
    }

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

    private void aguardarEscrita(com.google.api.core.ApiFuture<Void> future, String operacao, String id) {
        try {
            future.get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
        } catch (java.util.concurrent.ExecutionException e) {
            throw new RuntimeException("Falha ao " + operacao + " sugestão id=" + id, e.getCause());
        } catch (java.util.concurrent.TimeoutException e) {
            throw new RuntimeException("Timeout ao " + operacao + " sugestão id=" + id, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operação interrompida ao " + operacao + " sugestão id=" + id, e);
        }
    }

    private int parsarInt(Object valor, int defaultValue) {
        if (valor == null) return defaultValue;
        if (valor instanceof Number n) {
            long lv = n.longValue();
            if (lv < 0 || lv > Integer.MAX_VALUE) return defaultValue;
            return (int) lv;
        }
        try { return Integer.parseInt(valor.toString()); }
        catch (NumberFormatException e) { return defaultValue; }
    }
}
