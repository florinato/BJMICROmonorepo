package com.bjpractice.game_core.exception;



public class UnauthorizedActionException extends RuntimeException {

    /**
     * Un constructor simple que solo pide el mensaje de error.
     * @param message El mensaje que describe por qué se lanzó la excepción.
     */
    public UnauthorizedActionException(String message) {
        super(message);
    }
}