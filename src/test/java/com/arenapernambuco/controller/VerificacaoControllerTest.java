package com.arenapernambuco.controller;

import com.arenapernambuco.config.RateLimitFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("memory")
class VerificacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() {
        rateLimitFilter.limparContadores();
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void formulario_retorna200() throws Exception {
        mockMvc.perform(get("/verificar"))
                .andExpect(status().isOk())
                .andExpect(view().name("verificar"));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void verificar_codigoValido_retornaEventoAtivo() throws Exception {
        mockMvc.perform(post("/verificar")
                        .param("codigo", "AP-FUT-001")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("submitted", true))
                .andExpect(model().attribute("tipo", "evento_ativo"))
                .andExpect(model().attributeExists("evento"));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void verificar_codigoInativo_retornaAvisoDeEventoInativo() throws Exception {
        mockMvc.perform(post("/verificar")
                        .param("codigo", "AP-CUL-003")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("submitted", true))
                .andExpect(model().attribute("tipo", "evento_inativo"))
                .andExpect(model().attributeExists("evento"))
                .andExpect(content().string(containsString("Evento inativo")))
                .andExpect(content().string(containsString("Busque contato com o suporte")));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void verificar_codigoInexistente_retornaNaoEncontrado() throws Exception {
        mockMvc.perform(post("/verificar")
                        .param("codigo", "XXXX")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("submitted", true))
                .andExpect(model().attribute("tipo", "nao_encontrado"));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void verificar_codigoVazio_retornaTipoVazio() throws Exception {
        mockMvc.perform(post("/verificar")
                        .param("codigo", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("submitted", true))
                .andExpect(model().attribute("tipo", "vazio"));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void verificar_codigoComScriptInjection_rejeitado() throws Exception {
        mockMvc.perform(post("/verificar")
                        .param("codigo", "<script>alert(1)</script>")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("submitted", true))
                .andExpect(model().attribute("tipo", "vazio"));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void formulario_GET_naoExibeResultado() throws Exception {
        mockMvc.perform(get("/verificar"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("submitted"));
    }

    @Test
    void verificar_semLogin_redirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/verificar"))
                .andExpect(status().is3xxRedirection());
    }
}
