package com.arenapernambuco.dto;

public record EventoDTO(
        String id,
        String titulo,
        String dataFormatada,
        String categoria,
        String descricaoCurta,
        String descricaoCompleta,
        String imagemUrl,
        String badgeCor,
        boolean ativo,
        int capacidade,
        int inscritos
) {
}
