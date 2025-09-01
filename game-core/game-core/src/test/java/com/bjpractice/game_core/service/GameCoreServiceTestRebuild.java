package com.bjpractice.game_core.service;


import com.bjpractice.events.GameFinishedEvent;
import com.bjpractice.events.PlayerDoubleEvent;
import com.bjpractice.game_core.dto.GameDTO;
import com.bjpractice.game_core.exception.BetAlreadyInGameException;
import com.bjpractice.game_core.exception.GameNotFoundException;
import com.bjpractice.game_core.exception.InvalidGameActionException;
import com.bjpractice.game_core.exception.UnauthorizedActionException;
import com.bjpractice.game_core.kafka.producer.GameEventProducer;
import com.bjpractice.game_core.mapper.GameMapper;
import com.bjpractice.game_core.model.Card;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

        // El test no es determinista, si la partida termina (Rollo blck jack o black jack del dealer)
        // se enviará el evento a kafka si no, pues probamos que no se envió


        if (savedGame.getGameLogic().isGameOver()) {

            log.info("--> TEST SCENARIO: Game ended on deal. Verifying GameFinishedEvent was sent.");
            verify(gameEventProducer).sendGameFinishedEvent(any(GameFinishedEvent.class));

        } else {

            log.info("--> TEST SCENARIO: Game is ongoing. Verifying no event was sent.");
            verify(gameEventProducer, never()).sendGameFinishedEvent(any());
        }

    }


    @Test
    @DisplayName("startGame() should throw BetAlreadyInGameException when bet ID already exists")
    void startGame_whenBetIdExists_shouldThrowException() {

        // ARRANGE
        Long userId = 1L;
        UUID existingBetId = UUID.randomUUID();


        when(gameRepository.findByBetId(existingBetId)).thenReturn(Optional.of(new GameEntity()));

        // ACT & ASSERT

        assertThrows(BetAlreadyInGameException.class, () -> {
            gameCoreService.startGame(userId, existingBetId);
        });


        verify(gameRepository, never()).save(any());
        verify(gameEventProducer, never()).sendGameFinishedEvent(any());
    }

    @Test
    @DisplayName("playerHit() should update game when user is the owner and does not bust")
    void playerHit_whenUserIsOwner_shouldUpdateGameState() {

        // ARRANGE
        Long userId = 1L;
        UUID gameId = UUID.randomUUID();


        GameEntity gameInProgress = GameEntityTestBuilder.createGameInPlayerTurn(userId, UUID.randomUUID());


        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameInProgress));

        // ACT

        gameCoreService.playerHit(userId, gameId);

        // --- ASSERT ---
        // 4. Verify that the service first looked for the game.
        verify(gameRepository).findById(gameId);

        // 5. Verify that the service saved the updated game state.
        verify(gameRepository).save(any(GameEntity.class));

        // 6. Verify that NO game-over event was sent to Kafka.
        verify(gameEventProducer, never()).sendGameFinishedEvent(any());

        // 7. Verify that the mapper was called to return the DTO.
        verify(gameMapper).toDTO(any(GameEntity.class));

    }


    @Test
    @DisplayName("playerHit() should end game and publish event when player busts")
    void playerHit_whenPlayerBusts_shouldEndGameAndPublishEvent() {
        // --- ARRANGE ---
        Long userId = 1L;
        UUID gameId = UUID.randomUUID();
        UUID betId = UUID.randomUUID();


        List<Card> playerHand = List.of(new Card(Card.Suit.HEARTS,Card.Rank.QUEEN), new Card(Card.Suit.SPADES, Card.Rank.QUEEN));
        List<Card> dealerHand = List.of(new Card(Card.Suit.CLUBS, Card.Rank.SEVEN), new Card(Card.Suit.DIAMONDS, Card.Rank.EIGHT));
        List<Card> remainingDeck = List.of(new Card(Card.Suit.CLUBS, Card.Rank.TEN)); // The bust card

        GameEntity gameNearBust = GameEntityTestBuilder.createGameWithPredefinedDeck(
                userId, betId, playerHand, dealerHand, remainingDeck
        );


        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameNearBust));

        //  ACT

        gameCoreService.playerHit(userId, gameId);

        // ASSERT

        verify(gameRepository).save(any(GameEntity.class));


        verify(gameEventProducer).sendGameFinishedEvent(any(GameFinishedEvent.class));
    }

    @Test
    @DisplayName("playerHit() should throw UnauthorizedActionException when user is not the owner")
    void playerHit_whenUserIsNotOwner_shouldThrowException() {
        // ARRANGE
        Long ownerId = 1L;
        Long attackerId = 999L;
        UUID gameId = UUID.randomUUID();


        GameEntity gameOfAnotherUser = GameEntityTestBuilder.createGameInPlayerTurn(ownerId, UUID.randomUUID());


        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameOfAnotherUser));

        // ACT & ASSERT

        assertThrows(UnauthorizedActionException.class, () -> {
            gameCoreService.playerHit(attackerId, gameId);
        });


        verify(gameRepository, never()).save(any(GameEntity.class));
        verify(gameEventProducer, never()).sendGameFinishedEvent(any(GameFinishedEvent.class));
    }

    @Test
    @DisplayName("playerHit() should throw GameNotFoundException when game does not exist")
    void playerHit_whenGameNotFound_shouldThrowException() {
        // ARRANGE
        Long userId = 1L;
        UUID nonExistentGameId = UUID.randomUUID();


        when(gameRepository.findById(nonExistentGameId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(GameNotFoundException.class, () -> {
            gameCoreService.playerHit(userId, nonExistentGameId);
        });
    }


    // DOUBLE

    @Test
    @DisplayName("playerDouble() should save game and publish two events on success")
    void playerDouble_whenSuccessful_shouldSaveAndPublishTwoEvents() {
        //  ARRANGE

        Long userId = 1L;
        UUID gameId = UUID.randomUUID();
        UUID betId = UUID.randomUUID();

        List<Card> playerHand = List.of(new Card(Card.Suit.HEARTS, Card.Rank.FIVE), new Card(Card.Suit.SPADES, Card.Rank.SIX));
        List<Card> dealerHand = List.of(new Card(Card.Suit.CLUBS, Card.Rank.FOUR), new Card(Card.Suit.DIAMONDS, Card.Rank.TWO));


        List<Card> remainingDeck = List.of(
                new Card(Card.Suit.CLUBS, Card.Rank.TEN),
                new Card(Card.Suit.HEARTS, Card.Rank.KING),
                new Card(Card.Suit.DIAMONDS, Card.Rank.SEVEN)
        );

        GameEntity gameToDouble = GameEntityTestBuilder.createGameWithPredefinedDeck(
                userId, betId, playerHand, dealerHand, remainingDeck
        );

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameToDouble));

        // --- ACT ---

        gameCoreService.playerDouble(userId, gameId);

        // --- ASSERT ---

        verify(gameRepository).save(any(GameEntity.class));

        verify(gameEventProducer).sendPlayerDoubledEvent(any(PlayerDoubleEvent.class));
        verify(gameEventProducer).sendGameFinishedEvent(any(GameFinishedEvent.class));
    }


    @Test
    @DisplayName("playerDouble() should publish two events when player busts")
    void playerDouble_whenPlayerBusts_shouldPublishTwoEvents() {
        // ARRANGE
        Long userId = 1L;
        UUID gameId = UUID.randomUUID();
        UUID betId = UUID.randomUUID();


        List<Card> playerHand = List.of(new Card(Card.Suit.HEARTS, Card.Rank.SEVEN), new Card(Card.Suit.SPADES, Card.Rank.FIVE));
        List<Card> dealerHand = List.of(new Card(Card.Suit.CLUBS, Card.Rank.FOUR), new Card(Card.Suit.DIAMONDS, Card.Rank.TWO));
        List<Card> remainingDeck = List.of(new Card(Card.Suit.CLUBS, Card.Rank.TEN)); // The bust card

        GameEntity gameToBust = GameEntityTestBuilder.createGameWithPredefinedDeck(
                userId, betId, playerHand, dealerHand, remainingDeck
        );


        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameToBust));

        // ACT

        gameCoreService.playerDouble(userId, gameId);

        //  ASSERT

        verify(gameRepository).save(any(GameEntity.class));

        verify(gameEventProducer).sendPlayerDoubledEvent(any(PlayerDoubleEvent.class));
        verify(gameEventProducer).sendGameFinishedEvent(any(GameFinishedEvent.class));
    }

    @Test
    @DisplayName("playerDouble() should throw UnauthorizedActionException when user is not the owner")
    void playerDouble_whenUserIsNotOwner_shouldThrowException() {
        // --- ARRANGE ---
        Long ownerId = 1L;
        Long attackerId = 999L;
        UUID gameId = UUID.randomUUID();


        GameEntity gameOfAnotherUser = GameEntityTestBuilder.createGameInPlayerTurn(ownerId, UUID.randomUUID());


        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameOfAnotherUser));

        // ACT & ASSERT

        assertThrows(UnauthorizedActionException.class, () -> {
            gameCoreService.playerDouble(attackerId, gameId);
        });


        verify(gameRepository, never()).save(any(GameEntity.class));
        verify(gameEventProducer, never()).sendPlayerDoubledEvent(any());
        verify(gameEventProducer, never()).sendGameFinishedEvent(any());
    }

    @Test
    @DisplayName("playerDouble() should throw GameNotFoundException when game does not exist")
    void playerDouble_whenGameNotFound_shouldThrowException() {
        // --- ARRANGE ---
        Long userId = 1L;
        UUID nonExistentGameId = UUID.randomUUID();


        when(gameRepository.findById(nonExistentGameId)).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---

        assertThrows(GameNotFoundException.class, () -> {
            gameCoreService.playerDouble(userId, nonExistentGameId);
        });
    }

    @Test
    @DisplayName("playerDouble() should throw InvalidGameActionException when player has more than two cards")
    void playerDouble_whenPlayerHasMoreThanTwoCards_shouldThrowException() {
        // ARRANGE
        Long userId = 1L;
        UUID gameId = UUID.randomUUID();
        UUID betId = UUID.randomUUID();


        List<Card> playerHand = List.of(
                new Card(Card.Suit.HEARTS, Card.Rank.TWO),
                new Card(Card.Suit.SPADES, Card.Rank.THREE),
                new Card(Card.Suit.CLUBS, Card.Rank.FOUR) // The third card
        );
        List<Card> dealerHand = List.of(new Card(Card.Suit.CLUBS, Card.Rank.EIGHT), new Card(Card.Suit.DIAMONDS, Card.Rank.NINE));

        GameEntity gameWithThreeCards = GameEntityTestBuilder.createGameInProgressWithSpecificCards(
                userId, betId, playerHand, dealerHand
        );


        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameWithThreeCards));

        // ACT & ASSERT

        assertThrows(InvalidGameActionException.class, () -> {
            gameCoreService.playerDouble(userId, gameId);
        });


        verify(gameRepository, never()).save(any(GameEntity.class));
        verify(gameEventProducer, never()).sendPlayerDoubledEvent(any());
    }

    // STAND

    @Test
    @DisplayName("playerStand() should finish the game and publish an event")
    void playerStand_whenUserIsOwner_shouldFinishGameAndPublishEvent() {
        // --- ARRANGE ---
        Long userId = 1L;
        UUID gameId = UUID.randomUUID();
        UUID betId = UUID.randomUUID();


        List<Card> playerHand = List.of(new Card(Card.Suit.HEARTS, Card.Rank.KING), new Card(Card.Suit.SPADES, Card.Rank.QUEEN));
        List<Card> dealerHand = List.of(new Card(Card.Suit.CLUBS, Card.Rank.TEN), new Card(Card.Suit.DIAMONDS, Card.Rank.SIX));

        List<Card> remainingDeck = List.of(new Card(Card.Suit.CLUBS, Card.Rank.FIVE));

        GameEntity gameInProgress = GameEntityTestBuilder.createGameWithPredefinedDeck(
                userId, betId, playerHand, dealerHand, remainingDeck
        );


        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameInProgress));

        // ACT

        gameCoreService.playerStand(userId, gameId);

        // ASSERT

        verify(gameRepository).save(any(GameEntity.class));


        verify(gameEventProducer).sendGameFinishedEvent(any(GameFinishedEvent.class));
    }


    @Test
    @DisplayName("playerStand() should throw UnauthorizedActionException when user is not the owner")
    void playerStand_whenUserIsNotOwner_shouldThrowException() {
        // ARRANGE
        Long ownerId = 1L;
        Long attackerId = 999L;
        UUID gameId = UUID.randomUUID();


        GameEntity gameOfAnotherUser = GameEntityTestBuilder.createGameInPlayerTurn(ownerId, UUID.randomUUID());


        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameOfAnotherUser));

        // ACT & ASSERT

        assertThrows(UnauthorizedActionException.class, () -> {
            gameCoreService.playerStand(attackerId, gameId);
        });


        verify(gameRepository, never()).save(any(GameEntity.class));
        verify(gameEventProducer, never()).sendGameFinishedEvent(any());
    }


    @Test
    @DisplayName("playerStand() should throw GameNotFoundException when game does not exist")
    void playerStand_whenGameNotFound_shouldThrowException() {
        // ARRANGE
        Long userId = 1L;
        UUID nonExistentGameId = UUID.randomUUID();


        when(gameRepository.findById(nonExistentGameId)).thenReturn(Optional.empty());

        // ACT & ASSERT

        assertThrows(GameNotFoundException.class, () -> {
            gameCoreService.playerStand(userId, nonExistentGameId);
        });
    }










}








