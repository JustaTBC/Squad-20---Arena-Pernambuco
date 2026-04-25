package com.arenapernambuco.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class EventoFormDTO {

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 120, message = "Título deve ter no máximo 120 caracteres")
    private String titulo;

    @NotBlank(message = "Categoria é obrigatória")
    @Pattern(regexp = "Futebol|Música|Corporativo|Cultural|Teatro", message = "Categoria inválida")
    private String categoria;

    @NotBlank(message = "Data e horário são obrigatórios")
    private String dataHora;

    @Size(max = 240, message = "Descrição curta deve ter no máximo 240 caracteres")
    private String descricaoCurta;

    @Size(max = 2000, message = "Descrição completa deve ter no máximo 2000 caracteres")
    private String descricaoCompleta;

    @Pattern(regexp = "^$|https?://.+", message = "URL da imagem deve começar com http:// ou https://")
    private String imagemUrl;

    @Size(max = 20, message = "Código de verificação deve ter no máximo 20 caracteres")
    @Pattern(regexp = "^$|[A-Za-z0-9-]+", message = "Código de verificação deve conter apenas letras, números e hífen")
    private String codigoVerificacao;
    private boolean ativo = true;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getDataHora() { return dataHora; }
    public void setDataHora(String dataHora) { this.dataHora = dataHora; }

    public String getDescricaoCurta() { return descricaoCurta; }
    public void setDescricaoCurta(String descricaoCurta) { this.descricaoCurta = descricaoCurta; }

    public String getDescricaoCompleta() { return descricaoCompleta; }
    public void setDescricaoCompleta(String descricaoCompleta) { this.descricaoCompleta = descricaoCompleta; }

    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }

    public String getCodigoVerificacao() { return codigoVerificacao; }
    public void setCodigoVerificacao(String codigoVerificacao) { this.codigoVerificacao = codigoVerificacao; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
