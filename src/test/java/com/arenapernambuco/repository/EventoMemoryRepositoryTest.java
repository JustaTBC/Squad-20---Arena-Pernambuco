package com.arenapernambuco.repository;

import com.arenapernambuco.dto.EventoFiltroDTO;
import com.arenapernambuco.exception.EventoNaoEncontradoException;
import com.arenapernambuco.model.Evento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EventoMemoryRepositoryTest {

    private EventoMemoryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new EventoMemoryRepository();
    }

    @Test
    void buscarTodos_deveRetornarTodosOsEventos() {
        List<Evento> eventos = repository.buscarTodos();
        assertTrue(eventos.size() >= 8, "Deve retornar pelo menos 8 eventos");
    }

    @Test
    void buscarAtivos_deveRetornarApenasEventosAtivos() {
        List<Evento> ativos = repository.buscarAtivos();
        List<Evento> todos = repository.buscarTodos();

        assertTrue(ativos.size() < todos.size(), "Deve haver menos ativos que o total");
        assertTrue(ativos.stream().allMatch(Evento::ativo), "Todos devem estar ativos");
    }

    @Test
    void buscarPorId_existente_deveRetornarEvento() {
        Optional<Evento> evento = repository.buscarPorId("1");

        assertTrue(evento.isPresent(), "Evento com id '1' deve existir");
        assertEquals("1", evento.get().id());
    }

    @Test
    void buscarPorId_inexistente_deveRetornarVazio() {
        Optional<Evento> evento = repository.buscarPorId("999");

        assertTrue(evento.isEmpty(), "Evento inexistente deve retornar Optional vazio");
    }

    @Test
    void buscarPorCodigo_existente_deveRetornarEvento() {
        Optional<Evento> evento = repository.buscarPorCodigo("AP-FUT-001");

        assertTrue(evento.isPresent(), "Evento com codigo AP-FUT-001 deve existir");
        assertEquals("AP-FUT-001", evento.get().codigoVerificacao());
    }

    @Test
    void buscarPorCodigo_caseInsensitive_deveRetornarEvento() {
        Optional<Evento> evento = repository.buscarPorCodigo("ap-fut-001");

        assertTrue(evento.isPresent(), "Busca por codigo deve ser case insensitive");
    }

    @Test
    void buscarPorCodigo_nulo_deveRetornarVazio() {
        Optional<Evento> evento = repository.buscarPorCodigo(null);

        assertTrue(evento.isEmpty(), "Codigo nulo deve retornar Optional vazio");
    }

    @Test
    void buscarPorCodigo_vazio_deveRetornarVazio() {
        Optional<Evento> evento = repository.buscarPorCodigo("   ");

        assertTrue(evento.isEmpty(), "Codigo em branco deve retornar Optional vazio");
    }

    @Test
    void filtrar_porCategoria_deveRetornarApenasCategoria() {
        EventoFiltroDTO filtro = new EventoFiltroDTO("Futebol", null, null);
        List<Evento> resultado = repository.filtrar(filtro);

        assertFalse(resultado.isEmpty(), "Deve encontrar eventos de Futebol");
        assertTrue(
                resultado.stream().allMatch(e -> e.categoria().equalsIgnoreCase("Futebol")),
                "Todos devem ser da categoria Futebol"
        );
    }

    @Test
    void filtrar_porData_deveRetornarEventosDaData() {
        List<Evento> todos = repository.buscarAtivos();
        Evento primeiro = todos.get(0);
        LocalDate dataAlvo = primeiro.dataHora().toLocalDate();

        EventoFiltroDTO filtro = new EventoFiltroDTO(null, dataAlvo, null);
        List<Evento> resultado = repository.filtrar(filtro);

        assertFalse(resultado.isEmpty(), "Deve encontrar eventos na data");
        assertTrue(
                resultado.stream().allMatch(e -> e.dataHora().toLocalDate().equals(dataAlvo)),
                "Todos devem ser da data filtrada"
        );
    }

    @Test
    void filtrar_semFiltros_deveRetornarTodosAtivos() {
        EventoFiltroDTO filtro = new EventoFiltroDTO(null, null, null);
        List<Evento> resultado = repository.filtrar(filtro);
        List<Evento> ativos = repository.buscarAtivos();

        assertEquals(ativos.size(), resultado.size(), "Sem filtros deve retornar todos os ativos");
        assertTrue(resultado.stream().allMatch(Evento::ativo), "Todos devem estar ativos");
    }

    @Test
    void filtrar_ordemProximos_deveOrdenarPorDataAscendente() {
        EventoFiltroDTO filtro = new EventoFiltroDTO(null, null, "proximos");
        List<Evento> resultado = repository.filtrar(filtro);

        for (int i = 0; i < resultado.size() - 1; i++) {
            assertTrue(
                    !resultado.get(i).dataHora().isAfter(resultado.get(i + 1).dataHora()),
                    "Eventos devem estar em ordem crescente de data"
            );
        }
    }

    @Test
    void filtrar_ordemRecentes_deveOrdenarPorDataDescendente() {
        EventoFiltroDTO filtro = new EventoFiltroDTO(null, null, "recentes");
        List<Evento> resultado = repository.filtrar(filtro);

        for (int i = 0; i < resultado.size() - 1; i++) {
            assertTrue(
                    !resultado.get(i).dataHora().isBefore(resultado.get(i + 1).dataHora()),
                    "Eventos devem estar em ordem decrescente de data"
            );
        }
    }

    @Test
    void salvar_adicionaEventoRecuperavelPorId() {
        Evento novo = new Evento("99", "Novo Evento",
                LocalDateTime.of(2026, 6, 1, 19, 0),
                "Cultural", "COD-99", "Resumo", "Completa", "", true);

        repository.salvar(novo);

        Optional<Evento> recuperado = repository.buscarPorId("99");
        assertTrue(recuperado.isPresent());
        assertEquals("Novo Evento", recuperado.get().titulo());
    }

    @Test
    void salvar_comIdDuplicado_lancaIllegalArgumentException() {
        Evento duplicado = new Evento("1", "Duplicado",
                LocalDateTime.of(2026, 6, 1, 19, 0),
                "Cultural", "COD-DUP", "Resumo", "Completa", "", true);

        assertThrows(IllegalArgumentException.class, () -> repository.salvar(duplicado));
    }

    @Test
    void atualizar_alteraEventoExistente() {
        Evento atualizado = new Evento("1", "Título Atualizado",
                LocalDateTime.of(2026, 5, 10, 16, 0),
                "Futebol", "AP-FUT-001", "Nova descrição", "Nova completa", "", true);

        Evento resultado = repository.atualizar("1", atualizado);

        assertEquals("Título Atualizado", resultado.titulo());
        assertEquals("Título Atualizado", repository.buscarPorId("1").get().titulo());
    }

    @Test
    void atualizar_comIdInexistente_lancaEventoNaoEncontradoException() {
        Evento evento = new Evento("999", "Qualquer",
                LocalDateTime.of(2026, 6, 1, 19, 0),
                "Cultural", "COD-999", "", "", "", true);

        assertThrows(EventoNaoEncontradoException.class, () -> repository.atualizar("999", evento));
    }

    @Test
    void remover_removeEventoExistente() {
        repository.remover("1");

        assertTrue(repository.buscarPorId("1").isEmpty());
    }

    @Test
    void remover_comIdInexistente_lancaEventoNaoEncontradoException() {
        assertThrows(EventoNaoEncontradoException.class, () -> repository.remover("999"));
    }
}
