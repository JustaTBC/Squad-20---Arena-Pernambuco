package com.arenapernambuco.staticresources;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class LayoutCssTest {

    @Test
    void css_globalMantemHeaderStickySemCompensacaoDeHeaderFixo() throws IOException {
        String css = carregarCss();

        assertThat(css)
                .contains(".site-header {\n  position: sticky;")
                .contains("main {\n  flex: 1;\n  max-width: var(--max-width);\n  margin: 0 auto;\n  padding: 2rem 1.5rem 3rem;");
    }

    @Test
    void css_navCtaNaoRenderizaUnderlineDoMenu() throws IOException {
        String css = carregarCss();

        assertThat(css)
                .contains(".nav a.nav-cta::after,\n.nav a.nav-cta:hover::after,\n.nav a.nav-cta.active::after {\n  display: none;")
                .contains(".nav a.nav-cta:hover {\n  transform: translateY(-2px) scale(1.02);");
    }

    @Test
    void css_mantemBarraDeFiltrosAcimaDosCardsQuandoHaHover() throws IOException {
        String css = carregarCss();

        assertThat(css)
                .contains(".filtros {\n  display: flex;")
                .contains("  position: relative;\n  z-index: 2;")
                .contains(".eventos-grid {\n  display: grid;")
                .contains("  position: relative;\n  z-index: 1;");
    }

    private String carregarCss() throws IOException {
        try (var input = getClass().getResourceAsStream("/static/css/style.css")) {
            assertThat(input).isNotNull();
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
