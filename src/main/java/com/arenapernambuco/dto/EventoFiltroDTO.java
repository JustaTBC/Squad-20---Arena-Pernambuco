package com.arenapernambuco.dto;

import java.time.LocalDate;

public record EventoFiltroDTO(
        String categoria,
        LocalDate data,
        String ordem
) {
}
