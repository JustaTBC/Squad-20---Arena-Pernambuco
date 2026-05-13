package com.arenapernambuco.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EventoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleEventoNaoEncontrado(EventoNaoEncontradoException ex, Model model) {
        log.warn("Evento não encontrado: {}", ex.getEventoId());
        model.addAttribute("mensagem", "O evento solicitado não foi encontrado.");
        return "erro-404";
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(RuntimeException ex, Model model) {
        log.error("Erro interno [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        model.addAttribute("mensagem", "Ocorreu um erro inesperado. Tente novamente mais tarde.");
        return "erro-500";
    }
}
