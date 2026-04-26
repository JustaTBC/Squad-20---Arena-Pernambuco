package com.arenapernambuco.service;

import com.arenapernambuco.dto.CategoriaStatDTO;
import com.arenapernambuco.dto.DashboardDTO;
import com.arenapernambuco.model.Evento;
import com.arenapernambuco.repository.EventoRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsServiceTest {

    private static EventoRepository repoComEventos(List<Evento> eventos) {
        return new EventoRepository() {
            @Override public List<Evento> buscarTodos() { return eventos; }
            @Override public List<Evento> buscarAtivos() {
                return eventos.stream().filter(Evento::ativo).toList();
            }
            @Override public Optional<Evento> buscarPorId(String id) { return Optional.empty(); }
            @Override public Optional<Evento> buscarPorCodigo(String c) { return Optional.empty(); }
            @Override public Evento salvar(Evento e) { return e; }
            @Override public Evento atualizar(String id, Evento e) { return e; }
            @Override public void remover(String id) {}
        };
    }

    private static Evento evento(String id, String categoria, boolean ativo,
                                  int capacidade, int inscritos) {
        return new Evento(id, "Titulo " + id, LocalDateTime.now(),
                categoria, "COD" + id, "", "", "", ativo, capacidade, inscritos);
    }

    @Test
    void calcularDashboard_repositorioVazio_retornaZerosEListasVazias() {
        StatisticsService service = new StatisticsService(repoComEventos(List.of()));

        DashboardDTO dash = service.calcularDashboard();

        assertEquals(0, dash.totalAtivos());
        assertEquals("N/A", dash.categoriaMaisPopular());
        assertTrue(dash.statsPorCategoria().isEmpty());
        assertEquals(12, dash.semanaLabels().size());
        assertEquals(12, dash.eventosPorSemana().size());
        assertTrue(dash.eventosPorSemana().stream().allMatch(c -> c == 0L));
    }

    @Test
    void calcularDashboard_totalAtivos_contaApenasAtivos() {
        List<Evento> eventos = List.of(
                evento("1", "Futebol", true, 100, 80),
                evento("2", "Futebol", true, 100, 60),
                evento("3", "Futebol", false, 100, 0)
        );
        StatisticsService service = new StatisticsService(repoComEventos(eventos));

        DashboardDTO dash = service.calcularDashboard();

        assertEquals(2, dash.totalAtivos());
    }

    @Test
    void calcularDashboard_categoriaMaisPopular_categoriaComMaisAtivos() {
        List<Evento> eventos = List.of(
                evento("1", "Futebol", true, 100, 80),
                evento("2", "Futebol", true, 100, 60),
                evento("3", "Música", true, 100, 50)
        );
        StatisticsService service = new StatisticsService(repoComEventos(eventos));

        DashboardDTO dash = service.calcularDashboard();

        assertEquals("Futebol", dash.categoriaMaisPopular());
    }

    @Test
    void calcularDashboard_statsPorCategoria_calculaMediaMedianaDesvioPadrao() {
        // Futebol: evento A capacidade=100 inscritos=80 → taxa=80%
        //          evento B capacidade=200 inscritos=100 → taxa=50%
        // media = (80+50)/2 = 65.0
        // mediana (2 elementos): (50+80)/2 = 65.0
        // desvio padrão: sqrt( ((80-65)^2 + (50-65)^2) / 2 ) = sqrt(225) = 15.0
        List<Evento> eventos = List.of(
                new Evento("1", "T1", LocalDateTime.now(), "Futebol", "C1",
                        "", "", "", true, 100, 80),
                new Evento("2", "T2", LocalDateTime.now(), "Futebol", "C2",
                        "", "", "", true, 200, 100)
        );
        StatisticsService service = new StatisticsService(repoComEventos(eventos));

        DashboardDTO dash = service.calcularDashboard();

        assertEquals(1, dash.statsPorCategoria().size());
        CategoriaStatDTO stat = dash.statsPorCategoria().get(0);
        assertEquals("Futebol", stat.categoria());
        assertEquals(2, stat.count());
        assertEquals(65.0, stat.mediaOcupacao(), 0.001);
        assertEquals(65.0, stat.medianaOcupacao(), 0.001);
        assertEquals(15.0, stat.desvioPadrao(), 0.001);
    }

    @Test
    void calcularDashboard_statsPorCategoria_umEventoDesvioPadraoZero() {
        List<Evento> eventos = List.of(
                new Evento("1", "T1", LocalDateTime.now(), "Teatro", "C1",
                        "", "", "", true, 100, 60)
        );
        StatisticsService service = new StatisticsService(repoComEventos(eventos));

        DashboardDTO dash = service.calcularDashboard();

        CategoriaStatDTO stat = dash.statsPorCategoria().get(0);
        assertEquals(1, stat.count());
        assertEquals(60.0, stat.mediaOcupacao(), 0.001);
        assertEquals(60.0, stat.medianaOcupacao(), 0.001);
        assertEquals(0.0, stat.desvioPadrao(), 0.001);
    }

    @Test
    void calcularDashboard_statsPorCategoria_ignoraEventoComCapacidadeZero() {
        List<Evento> eventos = List.of(
                new Evento("1", "T1", LocalDateTime.now(), "Cultural", "C1",
                        "", "", "", true, 0, 0),
                new Evento("2", "T2", LocalDateTime.now(), "Cultural", "C2",
                        "", "", "", true, 100, 50)
        );
        StatisticsService service = new StatisticsService(repoComEventos(eventos));

        DashboardDTO dash = service.calcularDashboard();

        CategoriaStatDTO stat = dash.statsPorCategoria().get(0);
        assertEquals(2, stat.count());
        assertEquals(50.0, stat.mediaOcupacao(), 0.001);
        assertEquals(50.0, stat.medianaOcupacao(), 0.001);
        assertEquals(0.0, stat.desvioPadrao(), 0.001);
    }

    @Test
    void calcularDashboard_semanaLabels_retorna12Labels() {
        StatisticsService service = new StatisticsService(repoComEventos(List.of()));

        DashboardDTO dash = service.calcularDashboard();

        assertEquals(12, dash.semanaLabels().size());
        assertTrue(dash.semanaLabels().stream().allMatch(l -> l.startsWith("Sem ")));
    }

    @Test
    void calcularDashboard_eventosPorSemana_contaEventoNaSemanaCorreta() {
        LocalDateTime agora = LocalDateTime.now();
        List<Evento> eventos = List.of(
                new Evento("1", "T1", agora, "Futebol", "C1",
                        "", "", "", true, 100, 50)
        );
        StatisticsService service = new StatisticsService(repoComEventos(eventos));

        DashboardDTO dash = service.calcularDashboard();

        assertEquals(1L, dash.eventosPorSemana().get(11));
    }

    @Test
    void calcularDashboard_statsPorCategoria_permiteTaxaAcimaDe100() {
        // inscritos > capacidade é válido (lista de espera, sobreinscrição)
        List<Evento> eventos = List.of(
                new Evento("1", "T1", LocalDateTime.now(), "Futebol", "C1",
                        "", "", "", true, 100, 120)
        );
        StatisticsService service = new StatisticsService(repoComEventos(eventos));

        DashboardDTO dash = service.calcularDashboard();

        assertEquals(120.0, dash.statsPorCategoria().get(0).mediaOcupacao(), 0.001);
    }
}
