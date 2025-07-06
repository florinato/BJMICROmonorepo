package com.bjpractice.game_core.controller;


import com.bjpractice.game_core.dto.GameDTO;
import com.bjpractice.game_core.dto.StartGameRequest;
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
    public ResponseEntity<GameDTO> startGame(@RequestBody StartGameRequest request){

        GameDTO newGame = gameCoreService.startGame(request.userId(),request.betId());
        return ResponseEntity.ok(newGame);
    }


    @PostMapping("/{gameId}/stand")
    public ResponseEntity<GameDTO> playerStand(@PathVariable UUID gameId) {
        GameDTO updatedGame = gameCoreService.playerStand(gameId);
        return ResponseEntity.ok(updatedGame);
    }

    @PostMapping("/{gameId}/hit")
    public ResponseEntity<GameDTO> playerHit(@PathVariable UUID gameId) {
        GameDTO updatedGame = gameCoreService.playerHit(gameId);
        return ResponseEntity.ok(updatedGame);
    }
}
