package com.arenapernambuco.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("memory")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rotaPublica_portal_acessivelSemLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void rotaPublica_eventos_acessivelSemLogin() throws Exception {
        mockMvc.perform(get("/eventos"))
                .andExpect(status().isOk());
    }

    @Test
    void rotaPublica_eventosDetalhe_acessivelSemLogin() throws Exception {
        mockMvc.perform(get("/eventos/1"))
                .andExpect(status().isOk());
    }

    @Test
    void rotaProtegida_verificar_redirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/verificar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void rotaProtegida_verificar_acessivelComParticipante() throws Exception {
        mockMvc.perform(get("/verificar"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rotaProtegida_verificar_acessivelComAdmin() throws Exception {
        mockMvc.perform(get("/verificar"))
                .andExpect(status().isOk());
    }

    @Test
    void paginaLogin_acessivelSemLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }
}
