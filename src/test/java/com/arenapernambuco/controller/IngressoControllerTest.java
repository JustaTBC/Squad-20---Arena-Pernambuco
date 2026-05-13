package com.arenapernambuco.controller;

import com.arenapernambuco.dto.IngressoDTO;
import com.arenapernambuco.model.Evento;
import com.arenapernambuco.repository.EventoRepository;
import com.arenapernambuco.repository.IngressoMemoryRepository;
import com.arenapernambuco.service.IngressoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("memory")
class IngressoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IngressoService ingressoService;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private IngressoMemoryRepository ingressoMemoryRepository;

    @BeforeEach
    void resetarEvento1() {
        ingressoMemoryRepository.limpar();
        eventoRepository.atualizar("1", new Evento(
                "1", "Campeonato Pernambucano — Final",
                LocalDateTime.of(2026, 5, 10, 16, 0),
                "Futebol", "AP-FUT-001",
                "Grande final do Campeonato Pernambucano de 2026.",
                "A grande decisão do Campeonato Pernambucano acontece na Arena Pernambuco com portões abertos e transmissão ao vivo. Venha torcer pelo seu time!",
                "https://picsum.photos/seed/fut1/800/400", true, 45000, 38000));
    }

    @Test
    void listarIngressos_naoAutenticado_redirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/participante/ingressos"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "participante@arena.com", roles = "PARTICIPANTE")
    void listarIngressos_autenticado_retorna200ComAtributoIngressos() throws Exception {
        mockMvc.perform(get("/participante/ingressos"))
                .andExpect(status().isOk())
                .andExpect(view().name("participante/ingressos"))
                .andExpect(model().attributeExists("ingressos"));
    }

    @Test
    void comprar_naoAutenticado_redirecionaParaLogin() throws Exception {
        mockMvc.perform(post("/ingressos/comprar")
                        .with(csrf())
                        .param("eventoId", "1")
                        .param("quantidade", "2"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "participante@arena.com", roles = "PARTICIPANTE")
    void comprar_valido_redirecionaParaMeusIngressos() throws Exception {
        mockMvc.perform(post("/ingressos/comprar")
                        .with(csrf())
                        .param("eventoId", "1")
                        .param("quantidade", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/participante/ingressos"));
    }

    @Test
    @WithMockUser(username = "participante@arena.com", roles = "PARTICIPANTE")
    void comprar_quantidadeInvalida_redirecionaParaEventoComErro() throws Exception {
        mockMvc.perform(post("/ingressos/comprar")
                        .with(csrf())
                        .param("eventoId", "1")
                        .param("quantidade", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/eventos/1"));
    }

    @Test
    @WithMockUser(username = "participante@arena.com", roles = "PARTICIPANTE")
    void comprar_eventoInexistente_redirecionaParaEventosComErro() throws Exception {
        mockMvc.perform(post("/ingressos/comprar")
                        .with(csrf())
                        .param("eventoId", "9999")
                        .param("quantidade", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/eventos"));
    }

    @Test
    @WithMockUser(username = "participante@arena.com", roles = "PARTICIPANTE")
    void cancelar_ingressoExistente_redirecionaParaMeusIngressos() throws Exception {
        IngressoDTO ingresso = ingressoService.comprar("1", 1, "participante@arena.com");

        mockMvc.perform(post("/ingressos/" + ingresso.id() + "/cancelar")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/participante/ingressos"));
    }

    @Test
    @WithMockUser(username = "participante@arena.com", roles = "PARTICIPANTE")
    void cancelar_ingressoInexistente_redirecionaComErro() throws Exception {
        mockMvc.perform(post("/ingressos/id-inexistente/cancelar")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/participante/ingressos"));
    }
}
