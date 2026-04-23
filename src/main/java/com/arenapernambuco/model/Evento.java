package com.arenapernambuco.model;

import java.time.LocalDateTime;

public record Evento(
        String id,
        String titulo,
        LocalDateTime dataHora,
        String categoria,
        String codigoVerificacao,
        String descricaoCurta,
        String descricaoCompleta,
        String imagemUrl,
        boolean ativo
) {
}