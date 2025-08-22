package com.bjpractice.game_core.controller;


import com.bjpractice.game_core.dto.GameDTO;
import com.bjpractice.game_core.dto.StartGameBody;
import com.bjpractice.game_core.service.GameCoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/games")
public class GameController {

    private final GameCoreService gameCoreService;

    public GameController(GameCoreService gameCoreService) {
        this.gameCoreService = gameCoreService;
    }


    @PostMapping("/start")
    public ResponseEntity<GameDTO> startGame(
            @RequestHeader("X-User-ID") Long userId, // 1. Leemos el ID de la cabecera
            @RequestBody StartGameBody body) {      // 2. El cuerpo solo trae datos de negocio

        GameDTO newGame = gameCoreService.startGame(userId, body.betId());
        return ResponseEntity.ok(newGame);
    }


    @PostMapping("/{gameId}/stand")
    public ResponseEntity<GameDTO> playerStand(
            @RequestHeader("X-User-ID") Long userId, // 1. Leemos el ID
            @PathVariable UUID gameId) {

        // 2. Pasamos el userId al servicio para que pueda validar
        // que el usuario que hace la petición es el dueño del juego.
        GameDTO updatedGame = gameCoreService.playerStand(userId, gameId);
        return ResponseEntity.ok(updatedGame);
    }

    @PostMapping("/{gameId}/hit")
    public ResponseEntity<GameDTO> playerHit(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable UUID gameId) {

        GameDTO updatedGame = gameCoreService.playerHit(userId, gameId);
        return ResponseEntity.ok(updatedGame);
    }

    @PostMapping("/{gameId}/double")
    public ResponseEntity<GameDTO> doubleDown(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable UUID gameId) {

        GameDTO updatedGame = gameCoreService.playerDouble(userId, gameId);
        return ResponseEntity.ok(updatedGame);
    }
}
