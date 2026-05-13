package com.arenapernambuco.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class IngressoFormDTO {

    @NotBlank(message = "ID do evento é obrigatório")
    private String eventoId;

    @Min(value = 1, message = "Mínimo de 1 ingresso")
    @Max(value = 5, message = "Máximo de 5 ingressos por compra")
    private int quantidade = 1;

    public String getEventoId() {
        return eventoId;
    }

    public void setEventoId(String eventoId) {
        this.eventoId = eventoId;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }
}
