package com.arenapernambuco.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("memory")
class AdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void semLogin_dashboard_redirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void participante_dashboard_retorna403() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_dashboard_retorna200EViewCorreta() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("dash"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_dashboard_renderizaCardsKPI() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("dashboard-kpi")))
                .andExpect(content().string(containsString("Eventos Ativos")))
                .andExpect(content().string(containsString("Categoria Mais Popular")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_dashboard_renderizaTabelaEstatisticas() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("dashboard-stats-table")))
                .andExpect(content().string(containsString("Ocupação Média")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_dashboard_renderizaGraficoChartJs() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("chart.js")))
                .andExpect(content().string(containsString("eventosPorSemana")))
                .andExpect(content().string(containsString("new Chart(")))
                .andExpect(content().string(containsString("type: 'bar'")));
    }
}
