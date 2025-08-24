package com.bjpractice.game_core.dto;

import java.util.UUID;

/**
 * DTO  que modela el cuerpo de la petición para el endpoint
 *
 * Se utiliza para encapsular los parámetros de entrada en un objeto único,
 * proporcionando un contrato claro para la API, facilitando la validación
 * y mejorando la futura extensibilidad del controlador.
 */

// Es importante notar que el id viene de APISIX
public record StartGameBody(

        UUID betId)
{ }
