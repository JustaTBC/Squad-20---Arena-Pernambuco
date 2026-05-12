package com.arenapernambuco.repository;

import com.arenapernambuco.model.Sugestao;

import java.util.List;

public interface SugestaoRepository {
    Sugestao salvar(Sugestao sugestao);
    List<Sugestao> buscarTodas();
    void remover(String id);
}
