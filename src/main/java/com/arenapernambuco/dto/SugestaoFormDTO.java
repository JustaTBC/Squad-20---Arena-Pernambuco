package com.arenapernambuco.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SugestaoFormDTO {

    @NotBlank(message = "Tipo de evento é obrigatório")
    @Size(max = 100, message = "Tipo de evento deve ter no máximo 100 caracteres")
    private String tipoEvento;

    @NotBlank(message = "Descrição da ideia é obrigatória")
    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String descricaoIdeia;

    @Min(value = 1, message = "Público estimado deve ser maior que zero")
    private int publicoEstimado;

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }

    public String getDescricaoIdeia() { return descricaoIdeia; }
    public void setDescricaoIdeia(String descricaoIdeia) { this.descricaoIdeia = descricaoIdeia; }

    public int getPublicoEstimado() { return publicoEstimado; }
    public void setPublicoEstimado(int publicoEstimado) { this.publicoEstimado = publicoEstimado; }
}
