package com.bjpractice.game_core.service;


import com.bjpractice.game_core.config.TestKafkaConfiguration;
import com.bjpractice.game_core.dto.GameDTO;
import com.bjpractice.game_core.kafka.producer.GameEventProducer;
import com.bjpractice.game_core.model.GameEntity;
import com.bjpractice.game_core.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL"
})
@ContextConfiguration(classes = TestKafkaConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GameCoreServiceTest {



    // Inyección por constructor en lugar de Autowired por problemas con respecto a la presencia de beans reales y beans que son mocks
    private final GameCoreService gameCoreService;
    private final GameRepository gameRepository;
    private final GameEventProducer gameEventProducer;

    @Autowired
    public GameCoreServiceTest(GameCoreService gameCoreService, GameRepository gameRepository, GameEventProducer gameEventProducer){
        this.gameCoreService = gameCoreService;
        this.gameRepository = gameRepository;
        this.gameEventProducer = gameEventProducer;
    }





    @BeforeEach
    void setUp() {

        gameRepository.deleteAll();
    }

    // Happy Path
    @Test
    void startGame_whenBetIdIsNew_shouldCreateAndPersistGame() {

        // Arrange

        Long userId = 1L;
        UUID betId = UUID.randomUUID();

        // Act
        GameDTO createdGameDTO = gameCoreService.startGame(userId, betId);

        // Assert

        assertNotNull(createdGameDTO);
        assertNotNull(createdGameDTO.getGameId());
        assertEquals(userId, createdGameDTO.getUserId());

        Optional<GameEntity> savedGameEntityOptional = gameRepository.findByBetId(betId);
        assertTrue(savedGameEntityOptional.isPresent(), "La partida debería existir en la database");

        GameEntity savedGameEntity = savedGameEntityOptional.get();
        assertEquals(userId, savedGameEntity.getUserId());
        assertEquals(betId,savedGameEntity.getBetId());

        verify(gameEventProducer, never()).sendGameFinishedEvent(any());

    }


}
