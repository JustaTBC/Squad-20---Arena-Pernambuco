package com.arenapernambuco.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VerificacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void formulario_retorna200() throws Exception {
        mockMvc.perform(get("/verificar"))
                .andExpect(status().isOk())
                .andExpect(view().name("verificar"));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void verificar_codigoValido_retornaOk() throws Exception {
        mockMvc.perform(post("/verificar")
                        .param("codigo", "AP-FUT-001")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("ok", true))
                .andExpect(model().attributeExists("evento"));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void verificar_codigoInexistente_retornaFalse() throws Exception {
        mockMvc.perform(post("/verificar")
                        .param("codigo", "XXXX")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("ok", false));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void verificar_codigoVazio_retornaFalse() throws Exception {
        mockMvc.perform(post("/verificar")
                        .param("codigo", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("ok", false));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void verificar_codigoComScriptInjection_rejeitado() throws Exception {
        mockMvc.perform(post("/verificar")
                        .param("codigo", "<script>alert(1)</script>")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("ok", false));
    }

    @Test
    void verificar_semLogin_redirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/verificar"))
                .andExpect(status().is3xxRedirection());
    }
}
