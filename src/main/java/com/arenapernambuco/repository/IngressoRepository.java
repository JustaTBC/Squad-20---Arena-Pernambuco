package com.arenapernambuco.repository;

import com.arenapernambuco.model.Ingresso;

import java.util.List;
import java.util.Optional;

public interface IngressoRepository {
    Ingresso salvar(Ingresso ingresso);
    List<Ingresso> buscarPorUsername(String username);
    Optional<Ingresso> buscarPorId(String username, String id);
    Optional<Ingresso> buscarPorCodigo(String codigoIngresso);
    void cancelar(String username, String id);
}
