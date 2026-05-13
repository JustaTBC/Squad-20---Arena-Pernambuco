package com.arenapernambuco.repository;

import com.arenapernambuco.model.Ingresso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class IngressoMemoryRepositoryTest {

    private IngressoMemoryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new IngressoMemoryRepository();
    }

    private Ingresso ingresso(String id, String username, int qtd, boolean cancelado) {
        return new Ingresso(id, "evento-1", username, "COD" + id, qtd,
                LocalDateTime.of(2026, 5, 12, 10, 0), cancelado, null);
    }

    @Test
    void salvar_e_buscarPorUsername_retornaIngresso() {
        repository.salvar(ingresso("i1", "user@a.com", 2, false));
        List<Ingresso> result = repository.buscarPorUsername("user@a.com");
        assertEquals(1, result.size());
        assertEquals("i1", result.get(0).id());
        assertEquals(2, result.get(0).quantidade());
    }

    @Test
    void buscarPorUsername_semIngressos_retornaListaVazia() {
        List<Ingresso> result = repository.buscarPorUsername("ninguem@a.com");
        assertTrue(result.isEmpty());
    }

    @Test
    void buscarPorId_existente_retornaIngresso() {
        repository.salvar(ingresso("i2", "user@a.com", 1, false));
        Optional<Ingresso> result = repository.buscarPorId("user@a.com", "i2");
        assertTrue(result.isPresent());
        assertEquals("i2", result.get().id());
    }

    @Test
    void buscarPorId_inexistente_retornaVazio() {
        Optional<Ingresso> result = repository.buscarPorId("user@a.com", "nao-existe");
        assertTrue(result.isEmpty());
    }

    @Test
    void buscarPorId_usuarioErrado_retornaVazio() {
        repository.salvar(ingresso("i3", "user@a.com", 1, false));
        Optional<Ingresso> result = repository.buscarPorId("outro@a.com", "i3");
        assertTrue(result.isEmpty());
    }

    @Test
    void cancelar_marcaIngressoComoCancelado() {
        repository.salvar(ingresso("i4", "user@a.com", 3, false));
        repository.cancelar("user@a.com", "i4");
        Ingresso cancelado = repository.buscarPorId("user@a.com", "i4").orElseThrow();
        assertTrue(cancelado.cancelado());
        assertNotNull(cancelado.dataCancelamento());
        assertEquals(3, cancelado.quantidade());
    }

    @Test
    void buscarPorCodigo_existente_retornaIngresso() {
        Ingresso ing = new Ingresso("i-cod", "evento-1", "user@a.com", "ABCD1234", 1,
                LocalDateTime.of(2026, 5, 12, 10, 0), false, null);
        repository.salvar(ing);
        Optional<Ingresso> result = repository.buscarPorCodigo("ABCD1234");
        assertTrue(result.isPresent());
        assertEquals("i-cod", result.get().id());
    }

    @Test
    void buscarPorCodigo_inexistente_retornaVazio() {
        assertTrue(repository.buscarPorCodigo("ZZZZZZZZ").isEmpty());
    }

    @Test
    void cancelar_ingressoInexistente_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> repository.cancelar("user@a.com", "nao-existe"));
    }

    @Test
    void salvar_multiplosPorUsuario_retornaTodos() {
        repository.salvar(ingresso("i5", "user@a.com", 1, false));
        repository.salvar(ingresso("i6", "user@a.com", 2, false));
        assertEquals(2, repository.buscarPorUsername("user@a.com").size());
    }

    @Test
    void buscarPorUsername_isolaPorUsuario() {
        repository.salvar(ingresso("i7", "user1@a.com", 1, false));
        repository.salvar(ingresso("i8", "user2@a.com", 1, false));
        assertEquals(1, repository.buscarPorUsername("user1@a.com").size());
        assertEquals(1, repository.buscarPorUsername("user2@a.com").size());
    }
}
