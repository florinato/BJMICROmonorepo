package com.bjpractice.game_core.service;


import com.bjpractice.events.GameFinishedEvent;
import com.bjpractice.game_core.dto.GameDTO;
import com.bjpractice.game_core.kafka.producer.GameEventProducer;
import com.bjpractice.game_core.mapper.GameMapper;
import com.bjpractice.game_core.model.GameEntity;
import com.bjpractice.game_core.model.GameEntityTestBuilder;
import com.bjpractice.game_core.repository.GameRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@Slf4j
@ExtendWith(MockitoExtension.class)
public class GameCoreServiceTestRebuild {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GameEventProducer gameEventProducer;

    @Mock
    private GameMapper gameMapper;

    @InjectMocks
    private GameCoreService gameCoreService;

    private GameEntityTestBuilder testBuilder;

    @BeforeEach
    void setUp() {

        testBuilder = new GameEntityTestBuilder();


        lenient().when(gameMapper.toDTO(any(GameEntity.class)))
                .thenAnswer(invocation -> {
                    GameEntity entity = invocation.getArgument(0);


                    GameDTO dto = new GameDTO();


                    dto.setGameId(entity.getId());
                    dto.setUserId(entity.getUserId());
                    dto.setGameState(entity.getGameLogic().getState());

                    return dto;
                });
    }

    @Test
    @DisplayName("startGame() - Happy Path ✅")
    void startGame_whenBetIdIsNew_shouldCreateAndSaveGame() {
        // ARRANGE


        Long userId = 1L;
        UUID betId = UUID.randomUUID();


        GameEntity gameEntityToSave = testBuilder.createGameInPlayerTurn(userId, betId);


        when(gameRepository.findByBetId(betId)).thenReturn(Optional.empty());

        // ACT


        GameDTO resultDTO = gameCoreService.startGame(userId, betId);

        // ASSERT


        verify(gameRepository).findByBetId(betId);

        ArgumentCaptor<GameEntity> gameEntityCaptor = ArgumentCaptor.forClass(GameEntity.class);
        verify(gameRepository).save(gameEntityCaptor.capture());

        GameEntity savedGame = gameEntityCaptor.getValue();
        assertEquals(userId, savedGame.getUserId());
        assertEquals(betId, savedGame.getBetId());
        assertNotNull(savedGame.getId());

        verify(gameEventProducer, never()).sendGameFinishedEvent(any());


        assertNotNull(resultDTO);
        assertEquals(userId, resultDTO.getUserId());
    }

    @Test
    @DisplayName("startGame() should publish GameFinishedEvent if the game ends on deal")
    void startGame_whenGameEndsImmediately_shouldSaveAndPublishEvent() {

        // ARRANGE

        Long userId = 1L;
        UUID betId = UUID.randomUUID();

        when(gameRepository.findByBetId(any(UUID.class))).thenReturn(Optional.empty());


        // ACT

        gameCoreService.startGame(userId, betId);

        ArgumentCaptor<GameEntity> gameEntityCaptor = ArgumentCaptor.forClass(GameEntity.class);
        verify(gameRepository).save(gameEntityCaptor.capture());
        GameEntity savedGame = gameEntityCaptor.getValue();

        // ASSERT

        // El test no es determinista, si la partida termina (Rollo blck jack) se enviará el evento a kafka
        // si no, pues probamos que no se envió

        if (savedGame.getGameLogic().isGameOver()) {

            log.info("--> TEST SCENARIO: Game ended on deal. Verifying GameFinishedEvent was sent.");
            verify(gameEventProducer).sendGameFinishedEvent(any(GameFinishedEvent.class));

        } else {

            log.info("--> TEST SCENARIO: Game is ongoing. Verifying no event was sent.");
            verify(gameEventProducer, never()).sendGameFinishedEvent(any());
        }


    }


}








