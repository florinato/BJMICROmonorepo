package com.bjpractice.game_core.exception;

public class DeckIsEmptyException extends RuntimeException {

//    Motivos para el formato de esta exception
//
//    Conveniencia: Es más rápido y limpio escribir throw new DeckIsEmptyException();.
//    Mantenibilidad: Si en el futuro decides cambiar el texto del error, solo tienes que modificarlo en un sitio: la propia clase de la excepción.

    private static final String DEFAULT_MESSAGE = "The deck is empty. Cannot deal more cards.";


    public DeckIsEmptyException() {
        super(DEFAULT_MESSAGE);
    }


    public DeckIsEmptyException(String message) {
        super(message);
    }
}