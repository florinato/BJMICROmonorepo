package com.bjpractice.game_core.exception;

public class DeckIsEmptyException extends RuntimeException {

//    Motivos para el formato de esta exception
//
//    Conveniencia: Es más rápido y limpio escribir throw new DeckIsEmptyException();.
//    Consistencia: Te aseguras de que el mensaje de error para esta condición sea siempre el mismo en todos los logs y respuestas de la API, porque está definido en un único lugar (DEFAULT_MESSAGE). Esto se conoce como el principio de "Única Fuente de la Verdad" (Single Source of Truth).
//    Mantenibilidad: Si en el futuro decides cambiar el texto del error, solo tienes que modificarlo en un sitio: la propia clase de la excepción.

    private static final String DEFAULT_MESSAGE = "The deck is empty. Cannot deal more cards.";


    public DeckIsEmptyException() {
        super(DEFAULT_MESSAGE);
    }


    public DeckIsEmptyException(String message) {
        super(message);
    }
}