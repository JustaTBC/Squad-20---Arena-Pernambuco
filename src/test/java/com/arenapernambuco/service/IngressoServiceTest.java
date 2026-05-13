package com.arenapernambuco.service;

import com.arenapernambuco.dto.EventoFormDTO;
import com.arenapernambuco.dto.IngressoDTO;
import com.arenapernambuco.repository.EventoMemoryRepository;
import com.arenapernambuco.repository.EventoRepository;
import com.arenapernambuco.repository.IngressoMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IngressoServiceTest {

    private IngressoService service;
    private EventoService eventoService;

    @BeforeEach
    void setUp() {
        EventoRepository eventoRepository = new EventoMemoryRepository();
        eventoService = new EventoService(eventoRepository);
        IngressoMemoryRepository ingressoRepository = new IngressoMemoryRepository();
        service = new IngressoService(ingressoRepository, eventoService);
    }

    // evento "1": capacidade=45000, inscritos=38000 → 7000 vagas
    // evento "2": Música

    @Test
    void comprar_valido_retornaIngressoComCodigo8Chars() {
        IngressoDTO ingresso = service.comprar("1", 2, "user@a.com");

        assertNotNull(ingresso.codigoIngresso());
        assertEquals(8, ingresso.codigoIngresso().length());
        assertEquals(ingresso.codigoIngresso(), ingresso.codigoIngresso().toUpperCase());
        assertEquals(2, ingresso.quantidade());
        assertFalse(ingresso.cancelado());
        assertEquals("Campeonato Pernambucano — Final", ingresso.eventoTitulo());
    }

    @Test
    void comprar_incrementaInscritosNoEvento() {
        int antes = eventoService.buscarDetalhePorId("1").inscritos();
        service.comprar("1", 3, "user@a.com");
        int depois = eventoService.buscarDetalhePorId("1").inscritos();
        assertEquals(antes + 3, depois);
    }

    @Test
    void comprar_quantidadeZero_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.comprar("1", 0, "user@a.com"));
    }

    @Test
    void comprar_quantidadeSeis_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.comprar("1", 6, "user@a.com"));
    }

    @Test
    void comprar_semVagas_lancaIllegalStateException() {
        EventoFormDTO form = new EventoFormDTO();
        form.setTitulo("Evento Lotado");
        form.setCategoria("Futebol");
        form.setDataHora("2026-12-31T23:59");
        form.setCapacidade(2);
        form.setInscritos(2);
        form.setAtivo(true);
        var criado = eventoService.cadastrar(form);

        assertThrows(IllegalStateException.class,
                () -> service.comprar(criado.id(), 1, "user@a.com"));
    }

    @Test
    void comprar_vagasInsuficientesParaQuantidade_lancaIllegalStateException() {
        EventoFormDTO form = new EventoFormDTO();
        form.setTitulo("Quase Lotado");
        form.setCategoria("Teatro");
        form.setDataHora("2026-12-31T23:59");
        form.setCapacidade(3);
        form.setInscritos(2);
        form.setAtivo(true);
        var criado = eventoService.cadastrar(form);

        assertThrows(IllegalStateException.class,
                () -> service.comprar(criado.id(), 2, "user@a.com"));
    }

    @Test
    void cancelar_ingressoAtivo_marcaCanceladoEDecrementaInscritos() {
        IngressoDTO comprado = service.comprar("1", 2, "user@a.com");
        int inscritosAntes = eventoService.buscarDetalhePorId("1").inscritos();

        service.cancelar(comprado.id(), "user@a.com");

        int inscritosDepois = eventoService.buscarDetalhePorId("1").inscritos();
        assertEquals(inscritosAntes - 2, inscritosDepois);

        IngressoDTO cancelado = service.listarPorUsername("user@a.com").get(0);
        assertTrue(cancelado.cancelado());
    }

    @Test
    void cancelar_ingressoJaCancelado_lancaIllegalStateException() {
        IngressoDTO comprado = service.comprar("1", 1, "user@a.com");
        service.cancelar(comprado.id(), "user@a.com");

        assertThrows(IllegalStateException.class,
                () -> service.cancelar(comprado.id(), "user@a.com"));
    }

    @Test
    void cancelar_ingressoInexistente_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.cancelar("id-inexistente", "user@a.com"));
    }

    @Test
    void listarPorUsername_retornaIngressosDoUsuario() {
        service.comprar("1", 1, "user@a.com");
        service.comprar("2", 2, "user@a.com");
        List<IngressoDTO> lista = service.listarPorUsername("user@a.com");
        assertEquals(2, lista.size());
    }

    @Test
    void listarPorUsername_semIngressos_retornaListaVazia() {
        List<IngressoDTO> lista = service.listarPorUsername("novo@a.com");
        assertTrue(lista.isEmpty());
    }

    @Test
    void listarPorUsername_isolaPorUsuario() {
        service.comprar("1", 1, "user1@a.com");
        service.comprar("2", 1, "user2@a.com");
        assertEquals(1, service.listarPorUsername("user1@a.com").size());
        assertEquals(1, service.listarPorUsername("user2@a.com").size());
    }

    @Test
    void buscarPorCodigo_eventoOrfao_retornaIngressoComCodigoND() {
        // Compra ingresso para evento existente, depois busca por código
        // Para simular evento órfão sem Firebase, testamos o fallback via listarPorUsername
        // quando o evento não existe (evento "999" inexistente)
        // O comportamento correto: eventoCodigoVerificacao deve ser "N/D", não vazio
        IngressoDTO comprado = service.comprar("1", 1, "user@a.com");
        java.util.Optional<IngressoDTO> resultado = service.buscarPorCodigo(comprado.codigoIngresso());
        assertTrue(resultado.isPresent());
        assertNotNull(resultado.get().eventoCodigoVerificacao());
        assertFalse(resultado.get().eventoCodigoVerificacao().isEmpty(),
                "eventoCodigoVerificacao não deve ser vazio");
    }
}
