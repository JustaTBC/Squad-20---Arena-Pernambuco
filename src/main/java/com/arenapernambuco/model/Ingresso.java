package com.arenapernambuco.model;

import java.time.LocalDateTime;

public record Ingresso(
        String id,
        String eventoId,
        String username,
        String codigoIngresso,
        int quantidade,
        LocalDateTime dataCompra,
        boolean cancelado,
        LocalDateTime dataCancelamento
) {}
