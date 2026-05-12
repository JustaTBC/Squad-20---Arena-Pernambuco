package com.arenapernambuco.model;

import java.time.LocalDateTime;

public record Sugestao(
        String id,
        String tipoEvento,
        String descricaoIdeia,
        int publicoEstimado,
        StatusSugestao status,
        LocalDateTime criadaEm
) {
}
