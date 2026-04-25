package com.arenapernambuco.dto;

public record CategoriaStatDTO(
        String categoria,
        long count,
        double mediaOcupacao,
        double medianaOcupacao,
        double desvioPadrao
) {
}
