package com.bjpractice.bets.integration;

import com.bjpractice.bets.bet.model.BetEntity;
import com.bjpractice.bets.bet.model.BetStatus;
import com.bjpractice.bets.bet.repository.BetRepository;
import com.bjpractice.bets.config.properties.KafkaTopics;
import com.bjpractice.events.BetSettledEvent;
import com.bjpractice.events.GameFinishedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;




public class BetSettlementIntegrationTest extends AbstractIntegrationTest {


    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private BetRepository betRepository;

    @Autowired
    private KafkaTopics kafkaTopics;


    // Usamos una cola para capturar de forma síncrona el evento que produce nuestro servicio
    private final BlockingQueue<BetSettledEvent> betSettledEvents = new LinkedBlockingQueue<>();


    // Este listener se ejecuta en el contexto del test y añade los eventos a nuestra cola
    @KafkaListener(topics = "${app.kafka.topics.bet-settled}", groupId = "test-group")
    public void consumeBetSettledEvent(BetSettledEvent event) {
        betSettledEvents.add(event);
    }


    @BeforeEach
    void setUp() {
        // Limpiamos la base de datos y la cola antes de cada test
        betRepository.deleteAll();
        betSettledEvents.clear();
    }

    @Test
    void givenPlayerWins_whenGameFinishedEventIsConsumed_thenBetIsSettledAndEventIsProduced() throws InterruptedException {
        // Arrange: Crear una apuesta inicial en la base de datos
        BetEntity initialBet = BetEntity.builder()
                .userId(1L)
                .amount(new BigDecimal("10.00"))
                .status(BetStatus.PENDING_GAME)
                .build();
        betRepository.save(initialBet);

        GameFinishedEvent gameFinishedEvent = new GameFinishedEvent(
                UUID.randomUUID(),      // gameId
                initialBet.getId(),     // betId
                initialBet.getUserId(), // userId
                "PLAYER_WINS",          // result
                false                   // playerHasBlackjack
        );

        // Act: Producir el evento de juego finalizado, simulando a game-core-service
        kafkaTemplate.send(kafkaTopics.games(), gameFinishedEvent);

        // Assert: Verificar que el BetSettledEvent se produce y que la BBDD se actualiza

        // 1. Verificamos que se produce el evento de pago en Kafka
        BetSettledEvent receivedEvent = betSettledEvents.poll(10, TimeUnit.SECONDS);

        assertThat(receivedEvent).isNotNull();
        assertThat(receivedEvent.userId()).isEqualTo(initialBet.getUserId());
        // El pago es la apuesta original (10) + la ganancia (10) = 20
        assertThat(receivedEvent.amount()).isEqualByComparingTo(new BigDecimal("20.00"));

        // 2. Verificamos que el estado de la apuesta en la BBDD es 'WON'
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            BetEntity settledBet = betRepository.findById(initialBet.getId()).orElseThrow();
            assertThat(settledBet.getStatus()).isEqualTo(BetStatus.WON);
        });
    }

}

