//package com.bjpractice.game_core.service;
//
//import com.bjpractice.events.GameFinishedEvent;
//import com.bjpractice.game_core.config.properties.KafkaTopics;
//import com.bjpractice.game_core.integration.AbstractIntegrationTest;
//import com.bjpractice.events.PlayerDoubleEvent;
//import com.bjpractice.game_core.model.Card;
//import com.bjpractice.game_core.model.Game;
//import com.bjpractice.game_core.model.GameEntity;
//import com.bjpractice.game_core.model.GameEntityTestBuilder;
//import com.bjpractice.game_core.repository.GameRepository;
//import org.apache.kafka.clients.consumer.ConsumerRecords;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.TestPropertySource;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//@SpringBootTest
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
//@ActiveProfiles("test")
//class GameCoreServiceTCIntegrationTest extends AbstractIntegrationTest {
//
//    @Autowired
//    private KafkaTopics kafkaTopics;
//
//    @Autowired
//    private GameCoreService gameCoreService;
//
//    @Autowired
//    private GameRepository gameRepository;
//
//
//
//
//    @Test
//    @DisplayName("[TC] When player stands, should publish a real event to Kafka")
//    void playerStand_shouldPublishEventToKafkaContainer() {
//        // --- Arrange ---
//        GameEntity gameEntity = GameEntityTestBuilder.createGameInPlayerTurn(1L, UUID.randomUUID());
//        gameRepository.save(gameEntity);
//
//        // --- Act ---
//        gameCoreService.playerStand(gameEntity.getId());
//
//        // --- Assert ---
//        // ✅ CORRECCIÓN:
//        // 1. Llamamos a consumeEvents esperando UN (1) evento.
//        ConsumerRecords<String, Object> records = consumeEvents(kafkaTopics.games(), 1);
//
//        // 2. Sacamos el primer y único evento de la colección y lo convertimos al tipo correcto.
//        GameFinishedEvent receivedEvent = (GameFinishedEvent) records.iterator().next().value();
//
//        // 3. El resto de tus aserciones funcionan exactamente igual.
//        assertNotNull(receivedEvent);
//        assertEquals(gameEntity.getBetId(), receivedEvent.betId());
//        assertEquals(gameEntity.getUserId(), receivedEvent.userId());
//    }
//
//
//
//    @Test
//    @DisplayName("[TC] When player hits and busts, should publish GameFinishedEvent")
//    void playerHit_whenPlayerBusts_shouldPublishEventToKafka() {
//        // --- Arrange ---
//        // Creamos un estado de juego predefinido donde el siguiente hit causará un bust
//        List<Card> playerCards = List.of(new Card(Card.Suit.HEARTS, Card.Rank.TEN), new Card(Card.Suit.SPADES, Card.Rank.SEVEN));
//        List<Card> dealerCards = List.of(new Card(Card.Suit.CLUBS, Card.Rank.EIGHT));
//        List<Card> remainingDeck = List.of(new Card(Card.Suit.DIAMONDS, Card.Rank.KING)); // El hit será un Rey
//
//        GameEntity gameEntity = GameEntityTestBuilder.createGameWithPredefinedDeck(
//                1L, UUID.randomUUID(), playerCards, dealerCards, remainingDeck
//        );
//        gameRepository.save(gameEntity);
//
//        // --- Act ---
//        gameCoreService.playerHit(gameEntity.getId());
//
//        // --- Assert ---
//        // ✅ CORRECCIÓN: Usamos el nuevo helper robusto
//        // 1. Llamamos a consumeEvents esperando UN (1) evento.
//        ConsumerRecords<String, Object> records = consumeEvents(kafkaTopics.games(), 1);
//
//        // 2. Sacamos el evento de la colección y lo convertimos al tipo correcto.
//        GameFinishedEvent receivedEvent = (GameFinishedEvent) records.iterator().next().value();
//
//        // 3. Tus aserciones originales se quedan igual.
//        assertNotNull(receivedEvent);
//        assertEquals(gameEntity.getBetId(), receivedEvent.betId());
//        assertEquals(Game.GameResult.DEALER_WINS, receivedEvent.result(), "El resultado debería ser DEALER_WINS porque el jugador se pasó");
//    }
//
//
//    @Test
//    @DisplayName("[TC] When player doubles down, should publish PlayerDoubleEvent and GameFinishedEvent")
//    void playerDouble_shouldPublishTwoEvents() {
//
//        GameEntity gameEntity = GameEntityTestBuilder.createGameInPlayerTurn(1L, UUID.randomUUID());
//        gameRepository.save(gameEntity);
//
//        gameCoreService.playerDouble(gameEntity.getId());
//
//
//        ConsumerRecords<String, Object> records = consumeEvents(kafkaTopics.games(), 2);
//
//        PlayerDoubleEvent playerDoubleEvent = null;
//        GameFinishedEvent gameFinishedEvent = null;
//
//        for (var record : records) {
//            if (record.value() instanceof PlayerDoubleEvent) {
//                playerDoubleEvent = (PlayerDoubleEvent) record.value();
//            } else if (record.value() instanceof GameFinishedEvent) {
//                gameFinishedEvent = (GameFinishedEvent) record.value();
//            }
//        }
//
//        assertNotNull(playerDoubleEvent, "Se esperaba un PlayerDoubleEvent");
//        assertEquals(gameEntity.getBetId(), playerDoubleEvent.getBetId());
//
//        assertNotNull(gameFinishedEvent, "Se esperaba un GameFinishedEvent");
//        assertEquals(gameEntity.getBetId(), gameFinishedEvent.betId());
//
//    }
//}