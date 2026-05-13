package com.arenapernambuco.repository;

import com.arenapernambuco.model.Ingresso;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
public class IngressoMemoryRepository implements IngressoRepository {

    private final Map<String, Map<String, Ingresso>> store =
            Collections.synchronizedMap(new LinkedHashMap<>());

    @Override
    public Ingresso salvar(Ingresso ingresso) {
        store.computeIfAbsent(ingresso.username(), k -> new LinkedHashMap<>())
             .put(ingresso.id(), ingresso);
        return ingresso;
    }

    @Override
    public List<Ingresso> buscarPorUsername(String username) {
        return new ArrayList<>(store.getOrDefault(username, Map.of()).values());
    }

    @Override
    public Optional<Ingresso> buscarPorId(String username, String id) {
        return Optional.ofNullable(store.getOrDefault(username, Map.of()).get(id));
    }

    @Override
    public Optional<Ingresso> buscarPorCodigo(String codigoIngresso) {
        if (codigoIngresso == null) return Optional.empty();
        String upper = codigoIngresso.toUpperCase();
        return store.values().stream()
                .flatMap(m -> m.values().stream())
                .filter(i -> upper.equals(i.codigoIngresso()))
                .findFirst();
    }

    @Override
    public void cancelar(String username, String id) {
        Map<String, Ingresso> userMap = store.get(username);
        if (userMap == null || !userMap.containsKey(id)) {
            throw new IllegalArgumentException("Ingresso não encontrado: " + id);
        }
        Ingresso atual = userMap.get(id);
        userMap.put(id, new Ingresso(
                atual.id(), atual.eventoId(), atual.username(),
                atual.codigoIngresso(), atual.quantidade(), atual.dataCompra(),
                true, LocalDateTime.now()));
    }

    public void limpar() {
        store.clear();
    }
}
