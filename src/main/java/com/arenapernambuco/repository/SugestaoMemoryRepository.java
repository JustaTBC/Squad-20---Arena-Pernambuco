package com.arenapernambuco.repository;

import com.arenapernambuco.model.StatusSugestao;
import com.arenapernambuco.model.Sugestao;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class SugestaoMemoryRepository implements SugestaoRepository {

    private final List<Sugestao> sugestoes = Collections.synchronizedList(new ArrayList<>(List.of(
            new Sugestao("s1", "Festival de Forró",
                    "Um grande festival de forró com bandas locais pernambucanas",
                    5000, StatusSugestao.PENDENTE,
                    LocalDateTime.of(2026, 1, 10, 10, 0)),
            new Sugestao("s2", "Corrida de Rua Beneficente",
                    "Uma corrida beneficente no entorno da Arena com percurso de 5km e 10km",
                    2000, StatusSugestao.APROVADA,
                    LocalDateTime.of(2026, 2, 15, 9, 0))
    )));

    @Override
    public Sugestao salvar(Sugestao sugestao) {
        sugestoes.add(sugestao);
        return sugestao;
    }

    @Override
    public List<Sugestao> buscarTodas() {
        return Collections.unmodifiableList(sugestoes);
    }

    @Override
    public void remover(String id) {
        boolean removido = sugestoes.removeIf(s -> s.id().equals(id));
        if (!removido) {
            throw new IllegalArgumentException("Sugestão não encontrada: " + id);
        }
    }
}
