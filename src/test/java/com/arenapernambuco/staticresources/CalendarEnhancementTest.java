package com.arenapernambuco.staticresources;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class CalendarEnhancementTest {

    @Test
    void templates_marcamTodosOsCamposDeDataComCalendarioArena() throws IOException {
        assertThat(carregar("/templates/eventos.html"))
                .contains("type=\"date\"")
                .contains("data-arena-calendar");

        assertThat(carregar("/templates/admin/evento-form.html"))
                .contains("type=\"datetime-local\"")
                .contains("data-arena-calendar");
    }

    @Test
    void assets_expoemDatepickerCustomizadoDaArena() throws IOException {
        assertThat(carregar("/static/js/main.js"))
                .contains("initArenaCalendars")
                .contains("input.matches('input[type=\"date\"], input[type=\"datetime-local\"]')")
                .contains("arena-calendar__day")
                .contains("trigger.setAttribute('aria-labelledby'")
                .contains("trigger.setAttribute('aria-describedby'")
                .contains("trigger.setAttribute('aria-invalid'")
                .contains("event.composedPath")
                .contains("path.includes(field)")
                .contains("event.key === 'Escape'");

        assertThat(carregar("/static/css/style.css"))
                .contains(".arena-date-field")
                .contains(".arena-calendar")
                .contains(".arena-calendar__day.is-selected")
                .contains(".filtros select {\n  min-height: 40px;")
                .contains("linear-gradient(135deg, rgba(255, 107, 53, 0.08), rgba(124, 58, 237, 0.08))");
    }

    @Test
    void templateAdminCarregaScriptDoCalendarioArena() throws IOException {
        assertThat(carregar("/templates/admin/evento-form.html"))
                .contains("data-arena-calendar")
                .contains("th:src=\"@{/js/main.js}\"");
    }

    private String carregar(String resource) throws IOException {
        try (var input = getClass().getResourceAsStream(resource)) {
            assertThat(input).isNotNull();
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
