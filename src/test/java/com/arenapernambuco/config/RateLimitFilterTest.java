package com.arenapernambuco.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("memory")
class RateLimitFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void verificar_dentroDoLimite_retorna200() throws Exception {
        rateLimitFilter.limparContadores();
        mockMvc.perform(post("/verificar")
                        .param("codigo", "XXXX")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void verificar_excedeLimite_retorna429() throws Exception {
        rateLimitFilter.limparContadores();
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/verificar")
                    .param("codigo", "XXXX")
                    .with(csrf()));
        }
        mockMvc.perform(post("/verificar")
                        .param("codigo", "XXXX")
                        .with(csrf()))
                .andExpect(status().isTooManyRequests());
    }
}
