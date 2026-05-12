package com.arenapernambuco.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("memory")
class SugestaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void formulario_semLogin_retorna200() throws Exception {
        mockMvc.perform(get("/sugestoes"))
                .andExpect(status().isOk())
                .andExpect(view().name("sugestao-form"))
                .andExpect(model().attributeExists("form"));
    }

    @Test
    void confirmacao_semLogin_retorna200() throws Exception {
        mockMvc.perform(get("/sugestoes/confirmacao"))
                .andExpect(status().isOk())
                .andExpect(view().name("sugestao-confirmacao"));
    }

    @Test
    void postValido_redirecionaParaConfirmacao() throws Exception {
        mockMvc.perform(post("/sugestoes").with(csrf())
                        .param("tipoEvento", "Festival de Forró")
                        .param("descricaoIdeia", "Um grande festival de forró regional")
                        .param("publicoEstimado", "5000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sugestoes/confirmacao"));
    }

    @Test
    void postSemTipoEvento_retornaFormComErro() throws Exception {
        mockMvc.perform(post("/sugestoes").with(csrf())
                        .param("tipoEvento", "")
                        .param("descricaoIdeia", "Uma descrição válida")
                        .param("publicoEstimado", "1000"))
                .andExpect(status().isOk())
                .andExpect(view().name("sugestao-form"))
                .andExpect(model().attributeHasFieldErrors("form", "tipoEvento"));
    }

    @Test
    void postSemDescricao_retornaFormComErro() throws Exception {
        mockMvc.perform(post("/sugestoes").with(csrf())
                        .param("tipoEvento", "Show de Rock")
                        .param("descricaoIdeia", "")
                        .param("publicoEstimado", "1000"))
                .andExpect(status().isOk())
                .andExpect(view().name("sugestao-form"))
                .andExpect(model().attributeHasFieldErrors("form", "descricaoIdeia"));
    }

    @Test
    void postComPublicoZero_retornaFormComErro() throws Exception {
        mockMvc.perform(post("/sugestoes").with(csrf())
                        .param("tipoEvento", "Corrida")
                        .param("descricaoIdeia", "Uma corrida de rua")
                        .param("publicoEstimado", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("sugestao-form"))
                .andExpect(model().attributeHasFieldErrors("form", "publicoEstimado"));
    }

    @Test
    void postComPublicoNegativo_retornaFormComErro() throws Exception {
        mockMvc.perform(post("/sugestoes").with(csrf())
                        .param("tipoEvento", "Corrida")
                        .param("descricaoIdeia", "Uma corrida de rua")
                        .param("publicoEstimado", "-5"))
                .andExpect(status().isOk())
                .andExpect(view().name("sugestao-form"))
                .andExpect(model().attributeHasFieldErrors("form", "publicoEstimado"));
    }

    @Test
    void postSemCsrf_retorna403() throws Exception {
        mockMvc.perform(post("/sugestoes")
                        .param("tipoEvento", "Festival")
                        .param("descricaoIdeia", "Descrição")
                        .param("publicoEstimado", "1000"))
                .andExpect(status().isForbidden());
    }
}
