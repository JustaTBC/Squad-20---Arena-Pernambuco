package com.arenapernambuco.service;

import com.arenapernambuco.dto.CategoriaStatDTO;
import com.arenapernambuco.dto.DashboardDTO;
import com.arenapernambuco.model.Evento;
import com.arenapernambuco.repository.EventoRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final EventoRepository repository;

    public StatisticsService(EventoRepository repository) {
        this.repository = repository;
    }

    public DashboardDTO calcularDashboard() {
        LocalDate inicioJanela = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(11);
        List<Evento> todos  = repository.buscarTodos();
        List<Evento> ativos = repository.buscarAtivos();

        long totalAtivos = ativos.size();
        String categoriaMaisPopular = calcularCategoriaMaisPopular(ativos);
        List<CategoriaStatDTO> stats = calcularStatsPorCategoria(todos);
        List<String> labels   = calcularSemanaLabels(inicioJanela);
        List<Long> contagens  = calcularEventosPorSemana(todos, inicioJanela);

        return new DashboardDTO(totalAtivos, categoriaMaisPopular, stats, labels, contagens);
    }

    private String calcularCategoriaMaisPopular(List<Evento> ativos) {
        return ativos.stream()
                .collect(Collectors.groupingBy(Evento::categoria, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    private List<CategoriaStatDTO> calcularStatsPorCategoria(List<Evento> todos) {
        return todos.stream()
                .collect(Collectors.groupingBy(Evento::categoria))
                .entrySet().stream()
                .map(entry -> calcularStatCategoria(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(CategoriaStatDTO::categoria))
                .collect(Collectors.toList());
    }

    private CategoriaStatDTO calcularStatCategoria(String categoria, List<Evento> grupo) {
        long count = grupo.size();

        List<Double> taxas = grupo.stream()
                .filter(e -> e.capacidade() > 0)
                .map(e -> (double) e.inscritos() / e.capacidade() * 100.0)
                .sorted()
                .collect(Collectors.toList());

        double media  = taxas.isEmpty() ? 0.0
                : taxas.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double mediana = calcularMediana(taxas);
        double desvio  = calcularDesvioPadrao(taxas, media);

        return new CategoriaStatDTO(categoria, count, media, mediana, desvio);
    }

    private double calcularMediana(List<Double> ordenadas) {
        int n = ordenadas.size();
        if (n == 0) return 0.0;
        if (n % 2 == 1) return ordenadas.get(n / 2);
        return (ordenadas.get(n / 2 - 1) + ordenadas.get(n / 2)) / 2.0;
    }

    private double calcularDesvioPadrao(List<Double> taxas, double media) {
        return Math.sqrt(taxas.stream()
                .mapToDouble(t -> Math.pow(t - media, 2))
                .average()
                .orElse(0.0));
    }

    private List<String> calcularSemanaLabels(LocalDate inicioJanela) {
        DateTimeFormatter fmt  = DateTimeFormatter.ofPattern("dd/MM");
        List<String> labels    = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            labels.add("Sem " + inicioJanela.plusWeeks(i).format(fmt));
        }
        return labels;
    }

    private List<Long> calcularEventosPorSemana(List<Evento> todos, LocalDate inicioJanela) {
        List<Long> contagens   = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            LocalDate inicio = inicioJanela.plusWeeks(i);
            LocalDate fim    = inicio.plusDays(6);
            long count = todos.stream()
                    .filter(e -> {
                        LocalDate d = e.dataHora().toLocalDate();
                        return !d.isBefore(inicio) && !d.isAfter(fim);
                    })
                    .count();
            contagens.add(count);
        }
        return contagens;
    }
}
