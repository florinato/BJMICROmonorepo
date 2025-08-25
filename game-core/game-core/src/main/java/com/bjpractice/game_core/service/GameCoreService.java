package com.bjpractice.game_core.service;


import com.bjpractice.events.GameFinishedEvent;
import com.bjpractice.game_core.dto.GameDTO;
import com.bjpractice.game_core.exception.BetAlreadyInGameException;
import com.bjpractice.game_core.exception.GameNotFoundException;
import com.bjpractice.game_core.exception.UnauthorizedActionException;
import com.bjpractice.events.PlayerDoubleEvent;
import com.bjpractice.game_core.kafka.producer.GameEventProducer;
import com.bjpractice.game_core.mapper.GameMapper;
import com.bjpractice.game_core.model.Game;
import com.bjpractice.game_core.model.GameEntity;
import com.bjpractice.game_core.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;


// PENDING FOR TESTING AFTER APISIX IMPLEMENTATION
@Service
public class GameCoreService {

    private final GameRepository gameRepository;

    private final GameEventProducer gameEventProducer;

    private final GameMapper gameMapper;

    public GameCoreService(GameRepository gameRepository, GameEventProducer gameEventProducer, GameMapper gameMapper) {
        this.gameRepository = gameRepository;
        this.gameEventProducer = gameEventProducer;
        this.gameMapper = gameMapper;
    }



    // START GAME

    public GameDTO startGame(Long userId, UUID betId) {

        if (gameRepository.findByBetId(betId).isPresent()) {
            throw new BetAlreadyInGameException("Ya existe una partida asociada al betId: " + betId);
        }

        UUID gameId = UUID.randomUUID();
        GameEntity gameEntity = new GameEntity(gameId, userId, betId);
        Game game = gameEntity.getGameLogic();
        game.startGame();

        gameRepository.save(gameEntity);

        if (game.isGameOver()) {
            GameFinishedEvent event = new GameFinishedEvent(
                    gameEntity.getId(),
                    gameEntity.getBetId(),
                    gameEntity.getUserId(),
                    game.getResult().name(),
                    game.getPlayer().hasBlackjack()
            );
            gameEventProducer.sendGameFinishedEvent(event);
        }

        return gameMapper.toDTO(gameEntity);
    }

    // HIT

    public GameDTO playerHit(Long userId, UUID gameId) {

        GameEntity gameEntity = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada con id: " + gameId));

        if (!gameEntity.getUserId().equals(userId)) {

            throw new UnauthorizedActionException("El usuario " + userId + " no tiene permiso para actuar en la partida " + gameId);
        }

        Game game = gameEntity.getGameLogic();

        game.playerHit();

        gameRepository.save(gameEntity);

        // Kafka business por si player.isBust()
        if (game.isGameOver()) {
            GameFinishedEvent event = new GameFinishedEvent(
                    gameEntity.getId(),
                    gameEntity.getBetId(),
                    gameEntity.getUserId(),
                    game.getResult().name(),
                    game.getPlayer().hasBlackjack()
            );
            gameEventProducer.sendGameFinishedEvent(event);

        }

        return gameMapper.toDTO(gameEntity);

    }


    // STAND

    public GameDTO playerStand(Long userId, UUID gameId) {

        GameEntity gameEntity = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada con id: " + gameId));

        if (!gameEntity.getUserId().equals(userId)) {
            // Si no coinciden, lanzamos una excepción de acceso denegado.
            // Esto debería traducirse en un 403 Forbidden en tu GlobalExceptionHandler.
            throw new UnauthorizedActionException("El usuario " + userId + " no tiene permiso para actuar en la partida " + gameId);
        }


        Game game = gameEntity.getGameLogic();


        game.playerStand();


        gameRepository.save(gameEntity);


        if (game.isGameOver()) {
            GameFinishedEvent event = new GameFinishedEvent(
                    gameEntity.getId(),
                    gameEntity.getBetId(),
                    gameEntity.getUserId(),
                    game.getResult().name(),
                    game.getPlayer().hasBlackjack()
            );
            gameEventProducer.sendGameFinishedEvent(event);
        }


        return gameMapper.toDTO(gameEntity);
    }

    // DOUBLE

    public GameDTO playerDouble(Long userId, UUID gameId) {

        GameEntity gameEntity = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada con id " + gameId));

        Game game = gameEntity.getGameLogic();

        if (!gameEntity.getUserId().equals(userId)) {
            // Si no coinciden, lanzamos una excepción de acceso denegado.
            // Esto debería traducirse en un 403 Forbidden en tu GlobalExceptionHandler.
            throw new UnauthorizedActionException("El usuario " + userId + " no tiene permiso para actuar en la partida " + gameId);
        }

        game.playerDouble();

        PlayerDoubleEvent doubleEvent = new PlayerDoubleEvent(
                gameEntity.getId(),
                gameEntity.getBetId(),
                gameEntity.getUserId()

        );
        gameEventProducer.sendPlayerDoubledEvent(doubleEvent);

        gameRepository.save(gameEntity);


        if (game.isGameOver()) {
            GameFinishedEvent finishedEvent = new GameFinishedEvent(
                    gameEntity.getId(),
                    gameEntity.getBetId(),
                    gameEntity.getUserId(),
                    game.getResult().name(),
                    game.getPlayer().hasBlackjack()
            );
            gameEventProducer.sendGameFinishedEvent(finishedEvent);
        }

        return gameMapper.toDTO(gameEntity);

    }


}
