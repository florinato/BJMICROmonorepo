package com.bjpractice.game_core.service;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

//@SpringBootTest

//@AutoConfigureTestDatabase
//@TestPropertySource(properties = "spring.kafka.admin.enabled=false")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//class GameCoreServiceTest {
//
//    @Autowired
//    private GameCoreService gameCoreService;
//
//    @Autowired
//    private GameRepository gameRepository;
//
//    // ✅ 2. Reemplazamos el productor de Kafka real por un Mock
//    @MockBean
//    private GameEventProducer gameEventProducer;
//
//    @Autowired
//    public GameCoreServiceTest(GameCoreService gameCoreService, GameRepository gameRepository, GameEventProducer gameEventProducer) {
//        this.gameCoreService = gameCoreService;
//        this.gameRepository = gameRepository;
//        this.gameEventProducer = gameEventProducer;
//    }
//
//
//    @BeforeEach
//    void setUp() {
//
//        gameRepository.deleteAll();
//
//    }
//
//    // START GAME
//    @Test
//    void startGame_whenBetIdIsNew_shouldCreateAndPersistGame() {
//
//        // Arrange
//
//        Long userId = 1L;
//        UUID betId = UUID.randomUUID();
//
//        // Act
//        GameDTO createdGameDTO = gameCoreService.startGame(userId, betId);
//
//        // Assert
//
//        assertNotNull(createdGameDTO);
//        assertNotNull(createdGameDTO.getGameId());
//        assertEquals(userId, createdGameDTO.getUserId());
//
//        Optional<GameEntity> savedGameEntityOptional = gameRepository.findByBetId(betId);
//        assertTrue(savedGameEntityOptional.isPresent(), "La partida debería existir en la database");
//
//        GameEntity savedGameEntity = savedGameEntityOptional.get();
//        assertEquals(userId, savedGameEntity.getUserId());
//        assertEquals(betId, savedGameEntity.getBetId());
//
//        verify(gameEventProducer, never()).sendGameFinishedEvent(any());
//
//    }
//
//    // STAND
//
//    @Test
//    void playerStand_whenGameExists_shouldEndGameAndNotify() {
//
//
//        Long userId = 1L;
//        UUID betId = UUID.randomUUID();
//        GameEntity gameEntity = GameEntityTestBuilder.createGameInPlayerTurn(userId, betId);
//
//        gameRepository.save(gameEntity);
//
//        UUID gameId = gameEntity.getId();
//
//        GameDTO finalGameDTO = gameCoreService.playerStand(gameId);
//
//        // ASSERT:
//        // Verificamos que todo ha ocurrido como esperábamos en 3 niveles:
//        // DTO de respuesta, estado de la BBDD, y notificación a Kafka.
//
//        assertNotNull(finalGameDTO);
//        assertEquals(Game.GameState.GAME_OVER, finalGameDTO.getGameState(), "El estado del juego en el DTO debe ser GAME_OVER");
//        assertNotNull(finalGameDTO.getGameResult(), "El resultado del juego en el DTO no debe ser nulo");
//
//        // Kafka business with argumentCaptor (Which arguments where passed?)
//
//        ArgumentCaptor<GameFinishedEvent> eventCaptor = ArgumentCaptor.forClass(GameFinishedEvent.class);
//
//        verify(gameEventProducer, Mockito.times(1)).sendGameFinishedEvent(eventCaptor.capture());
//
//        // Verificamos que los datos del evento enviado a Kafka son los correctos
//        GameFinishedEvent capturedEvent = eventCaptor.getValue();
//        assertEquals(gameId, capturedEvent.getGameId());
//        assertEquals(betId, capturedEvent.getBetId());
//        assertEquals(userId, capturedEvent.getUserId());
//        assertEquals(finalGameDTO.getGameResult(), capturedEvent.getResult());
//
//    }
//
//    // HIT
//
//    @Test
//    void playerHit_whenGameIsInProgressAndPlayerDoesNotBust_shouldAddCardAndContinue() {
//
//        // Arrengium
//        Long userId = 1L;
//        UUID betId = UUID.randomUUID();
//
//        GameEntity gameEntity = GameEntityTestBuilder.createGameInPlayerTurn(userId, betId);
//        gameRepository.save(gameEntity);
//        UUID gameId = gameEntity.getId();
//
//        int initialCardCount = gameEntity.getGameLogic().getPlayer().getHand().size();
//
//        // Act
//
//        GameDTO resultDTO = gameCoreService.playerHit(gameId);
//
//
//        // Assert
//
//        assertNotNull(resultDTO);
//        assertEquals(initialCardCount + 1, resultDTO.getPlayerHand().size(), "El jugador debería tener una carta más");
//        assertEquals(Game.GameState.PLAYER_TURN, resultDTO.getGameState(), "Debería seguir siendo PLAYER_TURN");
//
//
//        GameEntity updatedEntity = gameRepository.findById(gameId).get();
//        assertEquals(initialCardCount + 1, updatedEntity.getGameLogic().getPlayer().getHand().size(), "Jugador debería tener una carta más (Database versh)");
//
//        verify(gameEventProducer, never()).sendGameFinishedEvent(any());
//
//
//    }
//
//    @Test
//    void playerHit_whenPlayerBusts_shouldEndGameAndNotify() {
//        // ARRANGE
//
//        Long userId = 1L;
//        UUID betId = UUID.randomUUID();
//
//
//        List<Card> playerCards = List.of(
//                new Card(Card.Suit.HEARTS, Card.Rank.TEN),
//                new Card(Card.Suit.SPADES, Card.Rank.FIVE)
//        );
//
//
//        List<Card> dealerCards = List.of(
//                new Card(Card.Suit.CLUBS, Card.Rank.EIGHT)
//        );
//
//
//        List<Card> remainingDeck = List.of(
//                new Card(Card.Suit.DIAMONDS, Card.Rank.KING)
//        );
//
//
//        GameEntity gameEntity = GameEntityTestBuilder.createGameWithPredefinedDeck(
//                userId, betId, playerCards, dealerCards, remainingDeck
//        );
//        gameRepository.save(gameEntity);
//        UUID gameId = gameEntity.getId();
//
//        // ACT
//
//        GameDTO resultDTO = gameCoreService.playerHit(gameId);
//
//        // ASSERT
//
//        assertEquals(Game.GameState.GAME_OVER, resultDTO.getGameState());
//        assertEquals(Game.GameResult.DEALER_WINS, resultDTO.getGameResult());
//        assertTrue(resultDTO.getPlayerScore() > 21, "La puntuación del jugador debería ser mayor a 21");
//
//        // Kafa notifico
//        ArgumentCaptor<GameFinishedEvent> eventCaptor = ArgumentCaptor.forClass(GameFinishedEvent.class);
//        verify(gameEventProducer, Mockito.times(1)).sendGameFinishedEvent(eventCaptor.capture());
//        assertEquals(Game.GameResult.DEALER_WINS, eventCaptor.getValue().getResult());
//    }
//
//
//
//    @Test
//    @DisplayName("When player doubles down, should deal one card, end game, and send two events")
//    void playerDoubleDown_whenAllowed_shouldEndGameAndNotify() {
//        // --- Arrange ---
//        Long userId = 1L;
//        UUID betId = UUID.randomUUID();
//
//        // Gracias builder prayge
//        GameEntity gameEntity = GameEntityTestBuilder.createGameInPlayerTurn(userId, betId);
//        gameRepository.save(gameEntity);
//        UUID gameId = gameEntity.getId();
//
//        int initialPlayerHandSize = gameEntity.getGameLogic().getPlayer().getHand().size();
//
//        // --- Act ---
//        GameDTO resultDTO = gameCoreService.playerDouble(gameId);
//
//        // --- Assert ---
//
//        assertEquals(initialPlayerHandSize + 1, resultDTO.getPlayerHand().size(), "El jugador debe tener una carta más");
//        assertEquals(Game.GameState.GAME_OVER, resultDTO.getGameState(), "El juego debería haber terminado");
//
//        // Verificamos que se enviaron AMBOS eventos a Kafka
//        ArgumentCaptor<PlayerDoubleEvent> doubleEventCaptor = ArgumentCaptor.forClass(PlayerDoubleEvent.class);
//        ArgumentCaptor<GameFinishedEvent> finishedEventCaptor = ArgumentCaptor.forClass(GameFinishedEvent.class);
//
//        // Verificamos que se llamó una vez a cada método del producer
//        verify(gameEventProducer, Mockito.times(1)).sendPlayerDoubledEvent(doubleEventCaptor.capture());
//        verify(gameEventProducer, Mockito.times(1)).sendGameFinishedEvent(finishedEventCaptor.capture());
//
//        // Verificamos el contenido de los eventos capturados
//        assertEquals(betId, doubleEventCaptor.getValue().getBetId(), "El betId del PlayerDoubleEvent es incorrecto");
//        assertEquals(betId, finishedEventCaptor.getValue().getBetId(), "El betId del GameFinishedEvent es incorrecto");
//        assertNotNull(finishedEventCaptor.getValue().getResult(), "El resultado del juego no puede ser nulo");
//    }
//
//
//
//
//}
