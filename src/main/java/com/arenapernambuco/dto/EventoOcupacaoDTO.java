package com.arenapernambuco.dto;

public record EventoOcupacaoDTO(
        String titulo,
        String categoria,
        int inscritos,
        int capacidade,
        double ocupacaoPct
) {
}
