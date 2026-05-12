package com.arenapernambuco.dto;

import com.arenapernambuco.model.StatusSugestao;

public record SugestaoDTO(
        String id,
        String tipoEvento,
        String descricaoIdeia,
        int publicoEstimado,
        StatusSugestao status,
        String criadaEmFormatada
) {
}
