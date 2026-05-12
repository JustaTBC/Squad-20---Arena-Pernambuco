package com.arenapernambuco.service;

import com.arenapernambuco.dto.SugestaoDTO;
import com.arenapernambuco.dto.SugestaoFormDTO;
import com.arenapernambuco.model.StatusSugestao;
import com.arenapernambuco.repository.SugestaoMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SugestaoServiceTest {

    private SugestaoService service;

    @BeforeEach
    void setUp() {
        service = new SugestaoService(new SugestaoMemoryRepository());
    }

    @Test
    void registrar_retornaDTOComIdGerado() {
        SugestaoDTO dto = service.registrar(formValido("Festival de Jazz", "Grande festival de jazz", 4000));
        assertNotNull(dto.id());
        assertFalse(dto.id().isBlank());
    }

    @Test
    void registrar_statusSemprePendente() {
        SugestaoDTO dto = service.registrar(formValido("Teatro", "Espetáculo teatral", 500));
        assertEquals(StatusSugestao.PENDENTE, dto.status());
    }

    @Test
    void registrar_camposTextoSaoSalvosCorretamente() {
        SugestaoDTO dto = service.registrar(formValido("  Show de Rock  ", "  Descrição  ", 1000));
        assertEquals("Show de Rock", dto.tipoEvento());
        assertEquals("Descrição", dto.descricaoIdeia());
    }

    @Test
    void registrar_publicoEstimadoECorretamente() {
        SugestaoDTO dto = service.registrar(formValido("Evento", "Desc", 7500));
        assertEquals(7500, dto.publicoEstimado());
    }

    @Test
    void registrar_criadaEmFormatadaNaoNula() {
        SugestaoDTO dto = service.registrar(formValido("Evento", "Desc", 100));
        assertNotNull(dto.criadaEmFormatada());
        assertFalse(dto.criadaEmFormatada().isBlank());
    }

    @Test
    void registrar_criadaEmFormatadaSegueFormato() {
        SugestaoDTO dto = service.registrar(formValido("Evento", "Desc", 100));
        assertTrue(dto.criadaEmFormatada().matches("\\d{2}/\\d{2}/\\d{4} às \\d{2}h\\d{2}"),
                "Formato esperado: dd/MM/yyyy às HHhMM, mas obteve: " + dto.criadaEmFormatada());
    }

    @Test
    void listarTodas_retornaListaNaoVazia() {
        List<SugestaoDTO> lista = service.listarTodas();
        assertFalse(lista.isEmpty());
    }

    @Test
    void listarTodas_retornaSugestaoRegistradaAnteriormente() {
        service.registrar(formValido("Circo", "Espetáculo circense", 2000));
        List<SugestaoDTO> lista = service.listarTodas();
        assertTrue(lista.stream().anyMatch(s -> s.tipoEvento().equals("Circo")));
    }

    private SugestaoFormDTO formValido(String tipo, String descricao, int publico) {
        SugestaoFormDTO form = new SugestaoFormDTO();
        form.setTipoEvento(tipo);
        form.setDescricaoIdeia(descricao);
        form.setPublicoEstimado(publico);
        return form;
    }
}
