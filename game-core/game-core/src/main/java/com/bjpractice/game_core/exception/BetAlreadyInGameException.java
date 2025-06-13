package com.bjpractice.game_core.exception;


import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // "No puedo procesar tu petición porque entra en conflicto con el estado actual del sistema"
public class BetAlreadyInGameException extends RuntimeException{

    public BetAlreadyInGameException(String message){
        super(message);
    }
}
