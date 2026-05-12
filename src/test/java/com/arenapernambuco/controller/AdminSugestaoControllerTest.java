package com.arenapernambuco.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("memory")
class AdminSugestaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void semLogin_redirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/admin/sugestoes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void participante_retorna403() throws Exception {
        mockMvc.perform(get("/admin/sugestoes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_listar_retorna200() throws Exception {
        mockMvc.perform(get("/admin/sugestoes"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/sugestoes-lista"))
                .andExpect(model().attributeExists("sugestoes"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_listar_sugestoesNaoVazio() throws Exception {
        mockMvc.perform(get("/admin/sugestoes"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("sugestoes"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_remover_existente_redireciona() throws Exception {
        mockMvc.perform(post("/admin/sugestoes/s1/remover").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/sugestoes"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_remover_inexistente_redireciona_comErro() throws Exception {
        mockMvc.perform(post("/admin/sugestoes/id-inexistente/remover").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/sugestoes"));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void participante_remover_retorna403() throws Exception {
        mockMvc.perform(post("/admin/sugestoes/s1/remover").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void semLogin_remover_redirecionaParaLogin() throws Exception {
        mockMvc.perform(post("/admin/sugestoes/s1/remover").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
