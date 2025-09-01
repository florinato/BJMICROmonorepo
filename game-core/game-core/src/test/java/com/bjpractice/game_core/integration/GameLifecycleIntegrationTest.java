package com.bjpractice.game_core.integration;

import com.bjpractice.events.GameFinishedEvent;
import com.bjpractice.events.PlayerDoubleEvent;
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

    private Consumer<String, Object> consumer;


    @BeforeEach
    void cleanup() {

        gameRepository.deleteAll();


        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(kafka.getBootstrapServers(), "test-group", "true");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.bjpractice.events");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // This allows the deserializer to determine the type from message headers
        consumerProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, "true");


        DefaultKafkaConsumerFactory<String, Object> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        consumer = cf.createConsumer();
        consumer.subscribe(java.util.Collections.singletonList("games.game-finished.test"));
    }

    @AfterEach
    void tearDown() {

        if (consumer != null) {
            consumer.close();
        }
    }


    @Test
    void startGame_shouldSaveToDbAndPublishEventOnBlackjack() {
        // ARRANGE
        Long userId = 1L;
        UUID betId = UUID.randomUUID();

        // ACT

        gameCoreService.startGame(userId, betId);

        //  ASSERT

        GameEntity savedGame = gameRepository.findByBetId(betId).orElseThrow();
        assertThat(savedGame.getUserId()).isEqualTo(userId);


        if (savedGame.getGameLogic().isGameOver()) {
            System.out.println("--> BLACKJACK! Verifying Kafka event...");


            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(100));
                assertThat(records.count()).isEqualTo(1);

                ConsumerRecord<String, Object> eventRecord = records.iterator().next();
                Object eventObject = eventRecord.value();

                assertThat(eventObject).isInstanceOf(GameFinishedEvent.class);

                GameFinishedEvent event = (GameFinishedEvent) eventObject;

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

        ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(200));
        assertThat(records.isEmpty())
                .as("No Kafka event should be published for a non-busting hit")
                .isTrue();
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
            ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(100));
            assertThat(records.count()).isEqualTo(1);

            ConsumerRecord<String, Object> eventRecord = records.iterator().next();
            Object eventObject = eventRecord.value();


            assertThat(eventObject).isInstanceOf(GameFinishedEvent.class);


            GameFinishedEvent event = (GameFinishedEvent) eventObject;

            assertThat(event.userId()).isEqualTo(userId);
            assertThat(event.betId()).isEqualTo(betId);
            assertThat(event.result()).isEqualTo("DEALER_WINS");
        });
    }

    // DOUBLE

    @Test
    @DisplayName("playerDouble() should save final state and publish two events")
    void playerDouble_whenSuccessful_shouldSaveToDbAndPublishTwoEvents() {

        // ARRANGE
        Long userId = 1L;
        UUID betId = UUID.randomUUID();


        List<Card> playerHand = List.of(new Card(Card.Suit.HEARTS, Card.Rank.FIVE), new Card(Card.Suit.SPADES, Card.Rank.SIX));
        List<Card> dealerHand = List.of(new Card(Card.Suit.CLUBS, Card.Rank.TEN));
        List<Card> remainingDeck = List.of(new Card(Card.Suit.CLUBS, Card.Rank.TEN), new Card(Card.Suit.DIAMONDS, Card.Rank.SEVEN)); // Player gets 10, dealer gets 7

        GameEntity initialGame = GameEntityTestBuilder.createGameWithPredefinedDeck(
                userId, betId, playerHand, dealerHand, remainingDeck
        );
        gameRepository.save(initialGame);

        // ACT
        gameCoreService.playerDouble(initialGame.getUserId(), initialGame.getId());

        // ASSERT

        GameEntity finalGame = gameRepository.findById(initialGame.getId()).orElseThrow();
        assertThat(finalGame.getGameLogic().getState()).isEqualTo(Game.GameState.GAME_OVER);
        assertThat(finalGame.getGameLogic().getResult()).isEqualTo(Game.GameResult.PLAYER_WINS);


        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(500));

            assertThat(records.count()).isEqualTo(2);

            boolean foundDoubleEvent = false;
            boolean foundFinishedEvent = false;

            for (ConsumerRecord<String, Object> record : records) {
                if (record.value() instanceof PlayerDoubleEvent event) {
                    foundDoubleEvent = true;
                    assertThat(event.userId()).isEqualTo(userId);
                    assertThat(event.betId()).isEqualTo(betId);
                } else if (record.value() instanceof GameFinishedEvent event) {
                    foundFinishedEvent = true;
                    assertThat(event.userId()).isEqualTo(userId);
                    assertThat(event.betId()).isEqualTo(betId);
                    assertThat(event.result()).isEqualTo("PLAYER_WINS");
                }
            }
            assertThat(foundDoubleEvent).isTrue();
            assertThat(foundFinishedEvent).isTrue();
        });
    }


}
