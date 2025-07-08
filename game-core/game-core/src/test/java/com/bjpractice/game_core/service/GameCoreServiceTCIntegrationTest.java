package com.bjpractice.game_core.service;

import com.bjpractice.game_core.integration.AbstractIntegrationTest;
import com.bjpractice.game_core.kafka.event.GameFinishedEvent;
import com.bjpractice.game_core.kafka.event.PlayerDoubleEvent;
import com.bjpractice.game_core.model.Card;
import com.bjpractice.game_core.model.Game;
import com.bjpractice.game_core.model.GameEntity;
import com.bjpractice.game_core.model.GameEntityTestBuilder;
import com.bjpractice.game_core.repository.GameRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class GameCoreServiceTCIntegrationTest extends AbstractIntegrationTest {

    @Value("${kafka.topic.games}")
    private String gamesTopic;

    @Autowired
    private GameCoreService gameCoreService;

    @Autowired
    private GameRepository gameRepository;

    /**
     * Método de ayuda para consumir un único evento de un topic de Kafka.
     * @param topic El topic del que se va a consumir.
     * @param eventType La clase del evento esperado, para una deserialización correcta.
     * @return El evento consumido.
     * @param <T> El tipo genérico del evento.
     */
    private <T> T consumeSingleEvent(String topic, Class<T> eventType) {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "tc-test-group-" + UUID.randomUUID());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        // Añadimos la configuración del tipo de evento esperado para que el deserializador funcione con genéricos
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, eventType);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, T> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(topic));

        ConsumerRecords<String, T> records = consumer.poll(Duration.ofSeconds(10));
        consumer.close();

        assertEquals(1, records.count(), "Se esperaba recibir exactamente un evento");
        return records.iterator().next().value();
    }

    @Test
    @DisplayName("[TC] When player stands, should publish a real event to Kafka")
    void playerStand_shouldPublishEventToKafkaContainer() {
        // --- Arrange ---
        GameEntity gameEntity = GameEntityTestBuilder.createGameInPlayerTurn(1L, UUID.randomUUID());
        gameRepository.save(gameEntity);

        // --- Act ---
        gameCoreService.playerStand(gameEntity.getId());

        // --- Assert ---

        // Aqui llamamod al helper method de arriba...
        GameFinishedEvent receivedEvent = consumeSingleEvent(gamesTopic, GameFinishedEvent.class);


        assertNotNull(receivedEvent);
        assertEquals(gameEntity.getBetId(), receivedEvent.getBetId());
        assertEquals(gameEntity.getUserId(), receivedEvent.getUserId());
    }



    @Test
    @DisplayName("[TC] When player hits and busts, should publish GameFinishedEvent")
    void playerHit_whenPlayerBusts_shouldPublishEventToKafka() {
        // --- Arrange ---
        // Creamos un estado de juego predefinido donde el siguiente hit causará un bust
        List<Card> playerCards = List.of(new Card(Card.Suit.HEARTS, Card.Rank.TEN), new Card(Card.Suit.SPADES, Card.Rank.SEVEN));
        List<Card> dealerCards = List.of(new Card(Card.Suit.CLUBS, Card.Rank.EIGHT));
        List<Card> remainingDeck = List.of(new Card(Card.Suit.DIAMONDS, Card.Rank.KING)); // El hit será un Rey

        GameEntity gameEntity = GameEntityTestBuilder.createGameWithPredefinedDeck(
                1L, UUID.randomUUID(), playerCards, dealerCards, remainingDeck
        );
        gameRepository.save(gameEntity);

        // --- Act ---
        gameCoreService.playerHit(gameEntity.getId());

        // --- Assert ---
        // Usamos nuestro helper. Esperamos un GameFinishedEvent con el resultado correcto.
        GameFinishedEvent receivedEvent = consumeSingleEvent(gamesTopic, GameFinishedEvent.class);

        assertNotNull(receivedEvent);
        assertEquals(gameEntity.getBetId(), receivedEvent.getBetId());
        assertEquals(Game.GameResult.DEALER_WINS, receivedEvent.getResult(), "El resultado debería ser DEALER_WINS porque el jugador se pasó");
    }


    @Test
    @DisplayName("[TC] When player doubles down, should publish PlayerDoubleEvent and GameFinishedEvent")
    void playerDouble_shouldPublishTwoEvents() {

        GameEntity gameEntity = GameEntityTestBuilder.createGameInPlayerTurn(1L, UUID.randomUUID());
        gameRepository.save(gameEntity);

        gameCoreService.playerDouble(gameEntity.getId());

        // Usamos el segundo helper para construir dos eventos
        ConsumerRecords<String, Object> records = consumeEvents(gamesTopic, 2);

        PlayerDoubleEvent playerDoubleEvent = null;
        GameFinishedEvent gameFinishedEvent = null;

        for (var record : records) {
            if (record.value() instanceof PlayerDoubleEvent) {
                playerDoubleEvent = (PlayerDoubleEvent) record.value();
            } else if (record.value() instanceof GameFinishedEvent) {
                gameFinishedEvent = (GameFinishedEvent) record.value();
            }
        }

        assertNotNull(playerDoubleEvent, "Se esperaba un PlayerDoubleEvent");
        assertEquals(gameEntity.getBetId(), playerDoubleEvent.getBetId());

        assertNotNull(gameFinishedEvent, "Se esperaba un GameFinishedEvent");
        assertEquals(gameEntity.getBetId(), gameFinishedEvent.getBetId());

    }
}
