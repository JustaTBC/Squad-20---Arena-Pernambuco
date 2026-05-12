package com.arenapernambuco.repository;

import com.arenapernambuco.model.StatusSugestao;
import com.arenapernambuco.model.Sugestao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SugestaoMemoryRepositoryTest {

    private SugestaoMemoryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new SugestaoMemoryRepository();
    }

    @Test
    void buscarTodas_retornaSeedsIniciais() {
        List<Sugestao> todas = repository.buscarTodas();
        assertTrue(todas.size() >= 2, "Deve ter ao menos 2 sugestões de seed");
    }

    @Test
    void buscarTodas_seedsTemStatusDiferentes() {
        List<Sugestao> todas = repository.buscarTodas();
        boolean temPendente = todas.stream().anyMatch(s -> s.status() == StatusSugestao.PENDENTE);
        boolean temAprovada = todas.stream().anyMatch(s -> s.status() == StatusSugestao.APROVADA);
        assertTrue(temPendente, "Deve haver ao menos uma sugestão PENDENTE");
        assertTrue(temAprovada, "Deve haver ao menos uma sugestão APROVADA");
    }

    @Test
    void salvar_adicionaSugestaoNaLista() {
        int tamanhoInicial = repository.buscarTodas().size();
        Sugestao nova = new Sugestao("s-test", "Show de Rock",
                "Um festival de rock local", 3000,
                StatusSugestao.PENDENTE, LocalDateTime.now());

        repository.salvar(nova);

        assertEquals(tamanhoInicial + 1, repository.buscarTodas().size());
    }

    @Test
    void salvar_retornaOMesmoObjeto() {
        Sugestao nova = new Sugestao("s-ret", "Maratona",
                "Corrida de 42km", 1000,
                StatusSugestao.PENDENTE, LocalDateTime.now());

        Sugestao retornada = repository.salvar(nova);

        assertEquals(nova.id(), retornada.id());
        assertEquals(nova.tipoEvento(), retornada.tipoEvento());
        assertEquals(nova.publicoEstimado(), retornada.publicoEstimado());
    }

    @Test
    void buscarTodas_retornaListaNaoModificavel() {
        List<Sugestao> todas = repository.buscarTodas();
        assertThrows(UnsupportedOperationException.class, () -> todas.add(
                new Sugestao("x", "X", "X", 1, StatusSugestao.PENDENTE, LocalDateTime.now())
        ));
    }

    @Test
    void remover_existente_reduzLista() {
        int tamanhoInicial = repository.buscarTodas().size();
        repository.remover("s1");
        assertEquals(tamanhoInicial - 1, repository.buscarTodas().size());
    }

    @Test
    void remover_inexistente_lancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> repository.remover("id-nao-existe"));
    }
}
