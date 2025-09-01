package com.bjpractice.game_core.integration;

import com.bjpractice.events.GameFinishedEvent;
import com.bjpractice.game_core.model.Card;
import com.bjpractice.game_core.model.Game;
import com.bjpractice.game_core.model.GameEntity;
import com.bjpractice.game_core.model.GameEntityTestBuilder;
import com.bjpractice.game_core.repository.GameRepository;
import com.bjpractice.game_core.service.GameCoreService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class GameLifecycleIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private GameCoreService gameCoreService;

    @Autowired
    private GameRepository gameRepository;

    private Consumer<String, GameFinishedEvent> consumer;

    // TODO: Necesitaremos un consumidor de Kafka para verificar los eventos.
    // Lo configuraremos en el siguiente paso.

    @BeforeEach
    void cleanup() {
        // Limpiamos la base de datos antes de cada test para asegurar el aislamiento.
        gameRepository.deleteAll();

        // 2. Configuramos y creamos el consumidor antes de cada test
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(kafka.getBootstrapServers(), "test-group", "true");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.bjpractice.events");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        DefaultKafkaConsumerFactory<String, GameFinishedEvent> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        consumer = cf.createConsumer();
        // Nos suscribimos al topic que nos interesa (asumiendo el nombre 'games-topic')
        consumer.subscribe(java.util.Collections.singletonList("games.game-finished.test"));
    }

    @AfterEach
    void tearDown() {
        // Cerramos el consumidor después de cada test
        if (consumer != null) {
            consumer.close();
        }
    }


    @Test
    void startGame_shouldSaveToDbAndPublishEventOnBlackjack() {
        // --- ARRANGE ---
        Long userId = 1L;
        UUID betId = UUID.randomUUID();

        // --- ACT ---
        // Llamamos al servicio. El test es no-determinista, así que cruzamos los dedos para un Blackjack.
        gameCoreService.startGame(userId, betId);

        // --- ASSERT ---
        // 1. Verificación de la Base de Datos (como antes)
        GameEntity savedGame = gameRepository.findByBetId(betId).orElseThrow();
        assertThat(savedGame.getUserId()).isEqualTo(userId);

        // 2. Verificación de Kafka (la nueva parte)
        if (savedGame.getGameLogic().isGameOver()) {
            System.out.println("--> BLACKJACK! Verifying Kafka event...");

            // Usamos Awaitility para esperar el mensaje
            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                ConsumerRecords<String, GameFinishedEvent> records = consumer.poll(Duration.ofMillis(100));
                assertThat(records.count()).isEqualTo(1);

                ConsumerRecord<String, GameFinishedEvent> eventRecord = records.iterator().next();
                GameFinishedEvent event = eventRecord.value();

                assertThat(event.userId()).isEqualTo(userId);
                assertThat(event.betId()).isEqualTo(betId);
                assertThat(event.gameId()).isEqualTo(savedGame.getId());
            });
        } else {
            System.out.println("--> No Blackjack this time. Skipping Kafka event verification.");
        }
    }


    // HIT

    @Test
    @DisplayName("playerHit() should update game in database when player does not bust")
    void playerHit_whenSuccessful_shouldUpdateGameInDatabase() {

        // ARRANGE
        Long userId = 1L;
        UUID betId = UUID.randomUUID();


        GameEntity initialGame = GameEntityTestBuilder.createGameInPlayerTurnDeterministicVersh(userId, betId);


        gameRepository.save(initialGame);

        // ACT

        gameCoreService.playerHit(initialGame.getUserId(), initialGame.getId());

        // ASSERT

        GameEntity updatedGame = gameRepository.findById(initialGame.getId()).orElseThrow();


        assertThat(updatedGame.getGameLogic().getPlayer().getHand().size()).isEqualTo(3);

        ConsumerRecords<String, GameFinishedEvent> records = consumer.poll(Duration.ofMillis(1000));
        assertThat(records.isEmpty()).isTrue();
    }

    // STAND

    @Test
    @DisplayName("playerStand() should save final state to DB and publish event")
    void playerStand_whenSuccessful_shouldSaveToDbAndPublishEvent() {

        // ARRANGE
        Long userId = 1L;
        UUID betId = UUID.randomUUID();


        List<Card> playerHand = List.of(new Card(Card.Suit.HEARTS, Card.Rank.KING), new Card(Card.Suit.SPADES, Card.Rank.QUEEN));
        List<Card> dealerHand = List.of(new Card(Card.Suit.CLUBS, Card.Rank.TEN), new Card(Card.Suit.DIAMONDS, Card.Rank.SIX));
        List<Card> remainingDeck = List.of(new Card(Card.Suit.CLUBS, Card.Rank.FIVE));

        GameEntity initialGame = GameEntityTestBuilder.createGameWithPredefinedDeck(
                userId, betId, playerHand, dealerHand, remainingDeck
        );


        gameRepository.save(initialGame);

        // ACT

        gameCoreService.playerStand(initialGame.getUserId(), initialGame.getId());

        // ASSERT

        GameEntity finalGame = gameRepository.findById(initialGame.getId()).orElseThrow();
        assertThat(finalGame.getGameLogic().getState()).isEqualTo(Game.GameState.GAME_OVER);
        assertThat(finalGame.getGameLogic().getResult()).isEqualTo(Game.GameResult.DEALER_WINS);


        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ConsumerRecords<String, GameFinishedEvent> records = consumer.poll(Duration.ofMillis(100));
            assertThat(records.count()).isEqualTo(1);

            ConsumerRecord<String, GameFinishedEvent> eventRecord = records.iterator().next();
            GameFinishedEvent event = eventRecord.value();

            assertThat(event.userId()).isEqualTo(userId);
            assertThat(event.betId()).isEqualTo(betId);
            assertThat(event.result()).isEqualTo("DEALER_WINS");
        });
    }


}
