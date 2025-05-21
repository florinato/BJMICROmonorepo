package com.bjpractice.game_core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GameCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(GameCoreApplication.class, args);
	}

/*
    TODO

    GIT INIT EN LA CARPETA MADRE PARA MONOROEPO

    Fase 1: Preparación del Terreno (Infraestructura)
    - Configurar conexión a SQL (MySQL)
    - Definir esquema de tablas para Game, PlayerSession, etc.
    - Configurar Flyway/Liquibase para migraciones
    - Configurar Kafka
    - Definir topics necesarios (game-events, bet-commands)
    - Crear producers/consumers básicos
    - Configurar APISIX (gateway)
    - Definir rutas para game-core
    - Configurar autenticación JWT

    Fase 2: Definición de Interfaces (DTOs y Eventos)
    - DTOs Principales (ya empezamos con CardDTO)
    - GameDTO (estado completo)
    - GameSummaryDTO (lista de partidas)
    - GameActionRequestDTO (hit/stand/double)
    - Eventos Kafka:
      - GameStartedEvent
      - GameFinishedEvent
      - BetValidationRequest (para consultar a bets)

    Fase 3: Capa de Servicio (Lógica de Negocio)
    - GameService (interfaz con los métodos clave)
    - GameServiceImpl (implementación)
      - startNewGame()
      - playerHit()
      - playerStand()
      - playerDouble()
    - Validación de estados (que no se pueda hit en estado incorrecto)

    Fase 4: Integraciones con otros Microservicios
    - Comunicación con Bets:
      - Validar apuesta al iniciar partida
      - Liquidar premios al terminar
    - Comunicación con User:
      - Emitir eventos de resultados (para estadísticas)
    - Patrón Circuit Breaker (para fallos en llamadas)

    Fase 5: Testing (Crítico en Microservicios)
    - Pruebas unitarias (lógica de blackjack pura)
    - Pruebas de integración (con Kafka y SQL)
    - Pruebas contractivas (Pact para compatibilidad)

    Fase 6: Deployment y Observabilidad
    - Configurar métricas (Prometheus/Grafana)
      - Partidas iniciadas/finalizadas
      - Tiempos de respuesta
    - Logs estructurados (ELK Stack)
    - Health Checks (para Kubernetes)

    Consejos Clave para Microservicios:
    - Principio FIRST:
      - Focused: game-core solo maneja lógica de juego
      - Independent: Debe funcionar aunque bets/user fallen
      - Resilient: Usar patrones como retry/circuit breaker
      - Scalable: Diseñar para escalar horizontalmente
      - Testable: Aislar dependencias externas en tests
    - Documentar:
      - API (OpenAPI/Swagger)
      - Eventos Kafka (esquemas JSON)
      - Diagrama de secuencia para flujos clave
    */

}
