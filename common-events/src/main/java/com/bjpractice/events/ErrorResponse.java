package com.bjpractice.events;

import java.time.Instant;

// DEUDA TECNICA: Usar esta idea para excepcionts entre micros


/**
 * Un DTO inmutable (record) para estandarizar las respuestas de error
 * en todos los microservicios.
 *
 * Incluye un constructor de conveniencia para facilitar su creación,
 * generando el timestamp automáticamente.
 */
//public record ErrorResponse(
//        int status,
//        String error,
//        String message
//
//){}