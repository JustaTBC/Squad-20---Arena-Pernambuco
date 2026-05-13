package com.arenapernambuco.dto;

import java.util.List;

public record DashboardDTO(
        long totalAtivos,
        String categoriaMaisPopular,
        List<CategoriaStatDTO> statsPorCategoria,
        List<String> semanaLabels,
        List<Long> eventosPorSemana,
        long totalInscritos,
        double ocupacaoMediaGeral,
        List<EventoOcupacaoDTO> topEventosPorOcupacao
) {
}
