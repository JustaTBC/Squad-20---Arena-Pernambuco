package com.arenapernambuco.service;

import com.arenapernambuco.dto.EventoDTO;
import com.arenapernambuco.dto.EventoFiltroDTO;
import com.arenapernambuco.dto.EventoFormDTO;
import com.arenapernambuco.exception.EventoNaoEncontradoException;
import com.arenapernambuco.repository.EventoMemoryRepository;
import com.arenapernambuco.repository.EventoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EventoServiceTest {

    private EventoService service;

    @BeforeEach
    void setUp() {
        EventoRepository repository = new EventoMemoryRepository();
        service = new EventoService(repository);
    }

    @Test
    void listarAtivos_retornaListaNaoVaziaComCamposPreenchidos() {
        List<EventoDTO> ativos = service.listarAtivos();

        assertFalse(ativos.isEmpty());
        for (EventoDTO dto : ativos) {
            assertNotNull(dto.id());
            assertNotNull(dto.titulo());
            assertNotNull(dto.dataFormatada());
            assertNotNull(dto.categoria());
            assertNotNull(dto.descricaoCurta());
            assertNotNull(dto.descricaoCompleta());
            assertNotNull(dto.imagemUrl());
            assertNotNull(dto.badgeCor());
        }
    }

    @Test
    void listarAtivos_naoIncluiEventosInativos() {
        List<EventoDTO> ativos = service.listarAtivos();

        boolean contemEvento10 = ativos.stream()
                .anyMatch(dto -> "10".equals(dto.id()));

        assertFalse(contemEvento10, "Evento com id '10' (inativo) nao deve aparecer na listagem");
        assertEquals(9, ativos.size());
    }

    @Test
    void filtrar_porCategoriaFutebol_retornaApenasFutebol() {
        EventoFiltroDTO filtro = new EventoFiltroDTO("Futebol", null, null);

        List<EventoDTO> resultado = service.filtrar(filtro);

        assertFalse(resultado.isEmpty());
        for (EventoDTO dto : resultado) {
            assertEquals("Futebol", dto.categoria());
        }
    }

    @Test
    void categoriasValidas_mantemAcentuacaoEmPortugues() {
        assertTrue(EventoService.CATEGORIAS_VALIDAS.contains("Música"));
        assertFalse(EventoService.CATEGORIAS_VALIDAS.contains("MÃºsica"));
    }

    @Test
    void filtrar_comCategoriaInvalida_retornaListaVazia() {
        EventoFiltroDTO filtro = new EventoFiltroDTO("CategoriaInexistente", null, null);

        List<EventoDTO> resultado = service.filtrar(filtro);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarDetalhePorId_comIdExistente_retornaEventoDTO() {
        EventoDTO dto = service.buscarDetalhePorId("1");

        assertNotNull(dto);
        assertEquals("1", dto.id());
        assertEquals("Campeonato Pernambucano — Final", dto.titulo());
        assertEquals("Futebol", dto.categoria());
    }

    @Test
    void buscarDetalhePorId_comIdInexistente_lancaEventoNaoEncontradoException() {
        assertThrows(EventoNaoEncontradoException.class, () -> service.buscarDetalhePorId("999"));
    }

    @Test
    void verificarPorCodigo_comCodigoExistente_retornaOptionalComEventoDTO() {
        Optional<EventoDTO> resultado = service.verificarPorCodigo("AP-FUT-001");

        assertTrue(resultado.isPresent());
        assertEquals("1", resultado.get().id());
        assertEquals("Futebol", resultado.get().categoria());
    }

    @Test
    void verificarPorCodigo_comCodigoInexistente_retornaOptionalVazio() {
        Optional<EventoDTO> resultado = service.verificarPorCodigo("CODIGO-INVALIDO");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void badgeCor_paraFutebol_retornaVerde() {
        EventoDTO dto = service.buscarDetalhePorId("1");

        assertEquals("#22c55e", dto.badgeCor());
    }

    @Test
    void badgeCor_paraMusica_retornaLaranja() {
        EventoDTO dto = service.buscarDetalhePorId("2");

        assertEquals("#FF6B35", dto.badgeCor());
    }

    @Test
    void dataFormatada_segueFormatoEsperado() {
        EventoDTO dto = service.buscarDetalhePorId("1");

        assertTrue(
                dto.dataFormatada().matches("\\d{2}/\\d{2}/\\d{4} às \\d{2}h\\d{2}"),
                "dataFormatada deve seguir o formato 'dd/MM/yyyy às HHhMM', mas foi: " + dto.dataFormatada()
        );
    }

    @Test
    void listarTodos_incluiEventosInativos() {
        List<EventoDTO> todos = service.listarTodos();
        // EventoMemoryRepository has 10 events, one inactive (id=10)
        assertEquals(10, todos.size());
    }

    @Test
    void cadastrar_comFormValido_retornaDTOComIdGerado() {
        EventoFormDTO form = new EventoFormDTO();
        form.setTitulo("Show de Teste");
        form.setCategoria("Música");
        form.setDataHora("2026-12-01T20:00");
        form.setDescricaoCurta("Resumo");
        form.setDescricaoCompleta("Completo");
        form.setImagemUrl("");
        form.setCodigoVerificacao("TST001");
        form.setAtivo(true);

        EventoDTO dto = service.cadastrar(form);

        assertNotNull(dto.id());
        assertEquals("Show de Teste", dto.titulo());
        assertEquals("Música", dto.categoria());
        assertTrue(dto.ativo());
    }

    @Test
    void cadastrar_semCodigoVerificacao_geraCodigoAutomatico() {
        EventoFormDTO form = new EventoFormDTO();
        form.setTitulo("Show Sem Código");
        form.setCategoria("Teatro");
        form.setDataHora("2026-12-01T20:00");
        form.setAtivo(true);

        EventoDTO dto = service.cadastrar(form);

        assertNotNull(dto.id());
        // verify the event was saved and can be retrieved
        EventoDTO recuperado = service.buscarDetalhePorId(dto.id());
        assertEquals("Show Sem Código", recuperado.titulo());
    }

    @Test
    void atualizar_comIdInexistente_lancaEventoNaoEncontradoException() {
        EventoFormDTO form = new EventoFormDTO();
        form.setTitulo("Qualquer");
        form.setCategoria("Cultural");
        form.setDataHora("2026-12-01T20:00");

        assertThrows(EventoNaoEncontradoException.class, () -> service.atualizar("999", form));
    }

    @Test
    void remover_comIdExistente_removeEvento() {
        service.remover("3");
        assertThrows(EventoNaoEncontradoException.class, () -> service.buscarDetalhePorId("3"));
    }

    @Test
    void remover_comIdInexistente_lancaEventoNaoEncontradoException() {
        assertThrows(EventoNaoEncontradoException.class, () -> service.remover("999"));
    }

    @Test
    void toFormDTO_mapeiaEventoCorretamente() {
        EventoFormDTO form = service.toFormDTO("1");

        assertEquals("Campeonato Pernambucano — Final", form.getTitulo());
        assertEquals("Futebol", form.getCategoria());
        assertEquals("AP-FUT-001", form.getCodigoVerificacao());
        assertTrue(form.isAtivo());
    }

    @Test
    void toFormDTO_comIdInexistente_lancaEventoNaoEncontradoException() {
        assertThrows(EventoNaoEncontradoException.class, () -> service.toFormDTO("999"));
    }
}
