package com.arenapernambuco.exception;

public class EventoNaoEncontradoException extends RuntimeException {

    private final String eventoId;

    public EventoNaoEncontradoException(String eventoId) {
        super("Evento não encontrado: " + eventoId);
        this.eventoId = eventoId;
    }

    public String getEventoId() {
        return eventoId;
    }
}
