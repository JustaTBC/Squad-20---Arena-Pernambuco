package com.arenapernambuco.dto;

public record IngressoDTO(
        String id,
        String eventoId,
        String eventoTitulo,
        String eventoDataFormatada,
        String eventoCategoria,
        String eventoImagemUrl,
        String badgeCor,
        String eventoCodigoVerificacao,
        String codigoIngresso,
        int quantidade,
        String dataCompraFormatada,
        boolean cancelado,
        String dataCancelamentoFormatada
) {}
