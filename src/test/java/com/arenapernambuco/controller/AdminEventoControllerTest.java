package com.arenapernambuco.controller;

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
class AdminEventoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void semLogin_adminEventos_redirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/admin/eventos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void participante_adminEventos_retorna403() throws Exception {
        mockMvc.perform(get("/admin/eventos"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_listarEventos_retorna200ComTodosEventos() throws Exception {
        mockMvc.perform(get("/admin/eventos"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/eventos-lista"))
                .andExpect(model().attributeExists("eventos"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_listarEventos_renderizaLayoutAdministrativoPolido() throws Exception {
        mockMvc.perform(get("/admin/eventos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("admin-page")))
                .andExpect(content().string(containsString("admin-toolbar")))
                .andExpect(content().string(containsString("admin-table")))
                .andExpect(content().string(containsString("admin-create-event")))
                .andExpect(content().string(containsString("Novo Evento")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_formNovo_retorna200() throws Exception {
        mockMvc.perform(get("/admin/eventos/novo"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/evento-form"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attributeExists("categorias"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_cadastrarEventoValido_redireciona() throws Exception {
        mockMvc.perform(post("/admin/eventos/novo")
                        .with(csrf())
                        .param("titulo", "Show de Teste")
                        .param("categoria", "Música")
                        .param("dataHora", "2026-12-01T20:00")
                        .param("descricaoCurta", "Descrição curta do show")
                        .param("descricaoCompleta", "Descrição completa do show de teste")
                        .param("imagemUrl", "https://picsum.photos/800/400")
                        .param("codigoVerificacao", "TST001")
                        .param("ativo", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/eventos"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_cadastrarSemDataHora_retornaFormComErro() throws Exception {
        mockMvc.perform(post("/admin/eventos/novo")
                        .with(csrf())
                        .param("titulo", "Show de Teste")
                        .param("categoria", "Música")
                        .param("dataHora", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/evento-form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("form", "dataHora"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_cadastrarSemTitulo_retornaFormComErro() throws Exception {
        mockMvc.perform(post("/admin/eventos/novo")
                        .with(csrf())
                        .param("titulo", "")
                        .param("categoria", "Música")
                        .param("dataHora", "2026-12-01T20:00"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/evento-form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("form", "titulo"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_cadastrarCategoriaInvalida_retornaFormComErro() throws Exception {
        mockMvc.perform(post("/admin/eventos/novo")
                        .with(csrf())
                        .param("titulo", "Show de Teste")
                        .param("categoria", "<script>alert(1)</script>")
                        .param("dataHora", "2026-12-01T20:00"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/evento-form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("form", "categoria"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_cadastrarImagemJavascript_retornaFormComErro() throws Exception {
        mockMvc.perform(post("/admin/eventos/novo")
                        .with(csrf())
                        .param("titulo", "Show de Teste")
                        .param("categoria", "Futebol")
                        .param("dataHora", "2026-12-01T20:00")
                        .param("imagemUrl", "javascript:alert(1)"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/evento-form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("form", "imagemUrl"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_formEditar_retorna200ComDadosPreenchidos() throws Exception {
        mockMvc.perform(get("/admin/eventos/1/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/evento-form"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attributeExists("eventoId"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_editarEventoValido_redireciona() throws Exception {
        mockMvc.perform(post("/admin/eventos/1/editar")
                        .with(csrf())
                        .param("titulo", "Campeonato Editado")
                        .param("categoria", "Futebol")
                        .param("dataHora", "2026-05-10T16:00")
                        .param("descricaoCurta", "Descrição editada")
                        .param("descricaoCompleta", "Descrição completa editada")
                        .param("imagemUrl", "https://picsum.photos/seed/fut1/800/400")
                        .param("codigoVerificacao", "AP-FUT-001")
                        .param("ativo", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/eventos"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_removerEvento_redireciona() throws Exception {
        mockMvc.perform(post("/admin/eventos/2/remover")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/eventos"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_removerEventoInexistente_retorna404() throws Exception {
        mockMvc.perform(post("/admin/eventos/999/remover")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
