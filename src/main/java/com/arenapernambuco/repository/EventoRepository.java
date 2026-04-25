package com.arenapernambuco.repository;

import com.arenapernambuco.dto.EventoFiltroDTO;
import com.arenapernambuco.model.Evento;

import java.util.List;
import java.util.Optional;

public interface EventoRepository {
    List<Evento> buscarTodos();
    List<Evento> buscarAtivos();
    Optional<Evento> buscarPorId(String id);
    Optional<Evento> buscarPorCodigo(String codigo);
    List<Evento> filtrar(EventoFiltroDTO filtro);

    Evento salvar(Evento evento);
    Evento atualizar(String id, Evento evento);
    void remover(String id);
}
