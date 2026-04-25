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
class EventoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listarEventos_retorna200() throws Exception {
        mockMvc.perform(get("/eventos"))
                .andExpect(status().isOk())
                .andExpect(view().name("eventos"))
                .andExpect(model().attributeExists("eventos"));
    }

    @Test
    void listarEventos_comFiltroCategoria() throws Exception {
        mockMvc.perform(get("/eventos").param("categoria", "Futebol"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("categoriaAtiva", "Futebol"));
    }

    @Test
    void listarEventos_categoriaInvalida_ignoraFiltro() throws Exception {
        mockMvc.perform(get("/eventos").param("categoria", "<script>alert(1)</script>"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("categoriaAtiva", (Object) null));
    }

    @Test
    void detalheEvento_existente_retorna200() throws Exception {
        mockMvc.perform(get("/eventos/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("evento-detalhe"))
                .andExpect(model().attributeExists("evento"));
    }

    @Test
    void detalheEvento_inexistente_retorna404() throws Exception {
        mockMvc.perform(get("/eventos/999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("erro-404"));
    }

    @Test
    void detalheEvento_idComPonto_retorna404() throws Exception {
        mockMvc.perform(get("/eventos/evento.child"))
                .andExpect(status().isNotFound());
    }

    @Test
    void detalheEvento_idComDolar_retorna404() throws Exception {
        mockMvc.perform(get("/eventos/evento$ref"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarEventos_comOrdenacao() throws Exception {
        mockMvc.perform(get("/eventos").param("ordem", "recentes"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("ordemAtiva", "recentes"));
    }

    @Test
    void listarEventos_ordemInvalida_usaPadrao() throws Exception {
        mockMvc.perform(get("/eventos").param("ordem", "invalido"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("ordemAtiva", "proximos"));
    }
}
