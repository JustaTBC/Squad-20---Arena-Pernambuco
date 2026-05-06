package com.arenapernambuco.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("memory")
class PortalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void portal_retorna200() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal"));
    }

    @Test
    void login_retorna200() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void login_exibeCredenciaisDeDemonstracao() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("participante@arena.com")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("senha123")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("admin@arena.com")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("admin123")));
    }
}
