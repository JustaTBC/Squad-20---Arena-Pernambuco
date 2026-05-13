package com.arenapernambuco.repository;

import com.arenapernambuco.model.Ingresso;
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
public class IngressoFirebaseRepository implements IngressoRepository {

    private static final Logger log = LoggerFactory.getLogger(IngressoFirebaseRepository.class);
    private static final long TIMEOUT_SEGUNDOS = 10;
    private static final DateTimeFormatter SERIALIZAR_FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final DatabaseReference ingressosRef;

    public IngressoFirebaseRepository(DatabaseReference ingressosRef) {
        this.ingressosRef = ingressosRef;
    }

    @Override
    public Ingresso salvar(Ingresso ingresso) {
        Map<String, Object> dados = ingressoParaMapa(ingresso);
        aguardarEscrita(
                ingressosRef.child(encodeUsername(ingresso.username()))
                        .child(ingresso.id()).setValueAsync(dados),
                "salvar", ingresso.id());
        return ingresso;
    }

    @Override
    public List<Ingresso> buscarPorUsername(String username) {
        DataSnapshot snapshot = lerSnapshot(ingressosRef.child(encodeUsername(username)));
        if (snapshot == null || !snapshot.exists()) {
            return Collections.emptyList();
        }
        List<Ingresso> lista = new ArrayList<>();
        for (DataSnapshot filho : snapshot.getChildren()) {
            try {
                lista.add(snapshotParaIngresso(filho, username));
            } catch (Exception e) {
                log.warn("Erro ao converter ingresso id={}: {}", filho.getKey(), e.getMessage());
            }
        }
        return lista;
    }

    @Override
    public Optional<Ingresso> buscarPorId(String username, String id) {
        DataSnapshot snapshot = lerSnapshot(
                ingressosRef.child(encodeUsername(username)).child(id));
        if (snapshot == null || !snapshot.exists()) return Optional.empty();
        try {
            return Optional.of(snapshotParaIngresso(snapshot, username));
        } catch (Exception e) {
            log.warn("Erro ao converter ingresso id={}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<Ingresso> buscarPorCodigo(String codigoIngresso) {
        if (codigoIngresso == null) return Optional.empty();
        String upper = codigoIngresso.toUpperCase();
        DataSnapshot todosRef = lerSnapshot(ingressosRef);
        if (todosRef == null || !todosRef.exists()) return Optional.empty();
        for (DataSnapshot userSnap : todosRef.getChildren()) {
            String username = decodeUsername(userSnap.getKey());
            for (DataSnapshot ingressoSnap : userSnap.getChildren()) {
                try {
                    Ingresso ingresso = snapshotParaIngresso(ingressoSnap, username);
                    if (upper.equals(ingresso.codigoIngresso())) return Optional.of(ingresso);
                } catch (Exception e) {
                    log.warn("Erro ao converter ingresso na busca por código: {}", e.getMessage());
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void cancelar(String username, String id) {
        Map<String, Object> atualizacoes = new LinkedHashMap<>();
        atualizacoes.put("cancelado", true);
        atualizacoes.put("dataCancelamento",
                LocalDateTime.now().format(SERIALIZAR_FORMATO));
        aguardarEscrita(
                ingressosRef.child(encodeUsername(username)).child(id)
                        .updateChildrenAsync(atualizacoes),
                "cancelar", id);
    }

    @SuppressWarnings("unchecked")
    private Ingresso snapshotParaIngresso(DataSnapshot snapshot, String username) {
        String id = snapshot.getKey();
        Object valor = snapshot.getValue();
        if (!(valor instanceof Map)) {
            throw new IllegalStateException("Formato inválido para ingresso id=" + id);
        }
        Map<String, Object> dados = (Map<String, Object>) valor;

        String eventoId = Objects.toString(dados.get("eventoId"), "");
        String codigo = Objects.toString(dados.get("codigoIngresso"), "");
        int quantidade = parsarInt(dados.get("quantidade"), 1);
        String dataCompraStr = Objects.toString(dados.get("dataCompra"), null);
        LocalDateTime dataCompra;
        try {
            dataCompra = LocalDateTime.parse(dataCompraStr, SERIALIZAR_FORMATO);
        } catch (DateTimeParseException | NullPointerException e) {
            dataCompra = LocalDateTime.now();
        }
        boolean cancelado = parsarBoolean(dados.get("cancelado"), false);
        String dataCancelamentoStr = Objects.toString(dados.get("dataCancelamento"), null);
        LocalDateTime dataCancelamento = null;
        if (dataCancelamentoStr != null) {
            try {
                dataCancelamento = LocalDateTime.parse(dataCancelamentoStr, SERIALIZAR_FORMATO);
            } catch (DateTimeParseException e) {
                log.warn("Ingresso id={} — não foi possível interpretar dataCancelamento='{}'", id, dataCancelamentoStr);
            }
        }
        return new Ingresso(id, eventoId, username, codigo, quantidade, dataCompra,
                cancelado, dataCancelamento);
    }

    private Map<String, Object> ingressoParaMapa(Ingresso ingresso) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("eventoId", ingresso.eventoId());
        mapa.put("username", ingresso.username());
        mapa.put("codigoIngresso", ingresso.codigoIngresso());
        mapa.put("quantidade", ingresso.quantidade());
        mapa.put("dataCompra", ingresso.dataCompra().format(SERIALIZAR_FORMATO));
        mapa.put("cancelado", ingresso.cancelado());
        if (ingresso.dataCancelamento() != null) {
            mapa.put("dataCancelamento", ingresso.dataCancelamento().format(SERIALIZAR_FORMATO));
        }
        return mapa;
    }

    static String encodeUsername(String username) {
        return username
                .replace("_", "_0")
                .replace(".", "_1")
                .replace("@", "_2");
    }

    static String decodeUsername(String encoded) {
        return encoded
                .replace("_2", "@")
                .replace("_1", ".")
                .replace("_0", "_");
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
            if (!latch.await(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)) {
                log.warn("Timeout ao aguardar Firebase ({}s)", TIMEOUT_SEGUNDOS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return resultado.get();
    }

    private void aguardarEscrita(com.google.api.core.ApiFuture<Void> future,
                                  String operacao, String id) {
        try {
            future.get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
        } catch (java.util.concurrent.ExecutionException e) {
            throw new RuntimeException("Falha ao " + operacao + " ingresso id=" + id, e.getCause());
        } catch (java.util.concurrent.TimeoutException e) {
            throw new RuntimeException("Timeout ao " + operacao + " ingresso id=" + id, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operação interrompida ao " + operacao + " ingresso id=" + id, e);
        }
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
            if (lv < 0 || lv > Integer.MAX_VALUE) return defaultValue;
            return (int) lv;
        }
        try { return Integer.parseInt(valor.toString()); }
        catch (NumberFormatException e) { return defaultValue; }
    }
}
