package com.bjpractice.bets.integration;

import com.bjpractice.bets.bet.model.BetEntity;
import com.bjpractice.bets.bet.model.BetStatus;
import com.bjpractice.bets.bet.repository.BetRepository;
import com.bjpractice.bets.client.UserServiceClient;
import com.bjpractice.bets.config.properties.KafkaTopics;

import com.bjpractice.events.GameFinishedEvent;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.kafka.config.KafkaListenerEndpointRegistry;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
class BetSettlementIntegrationTest extends AbstractIntegrationTest {


    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private BetRepository betRepository;

    @Autowired
    private KafkaTopics kafkaTopics;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;


    @Deprecated
    @Value("${spring.application.name}")
    private String applicationConsumerGroupId;

    @MockBean
    private UserServiceClient userServiceClient;


    @BeforeEach
    void setUp() {
        betRepository.deleteAll();
    }

    @Test
    void givenPlayerWins_whenGameFinishedEventIsConsumed_thenBetIsSettledAndEventIsProduced() throws InterruptedException {
        // Arrange
        BetEntity initialBet = BetEntity.builder()
                .userId(1L)
                .amount(new BigDecimal("10.00"))
                .status(BetStatus.PENDING_GAME)
                .build();
        betRepository.save(initialBet);

        GameFinishedEvent gameFinishedEvent = new GameFinishedEvent(
                UUID.randomUUID(),
                initialBet.getId(),
                initialBet.getUserId(),
                "PLAYER_WINS",
                false                   // playerHasBlackJack
        );

        await().atMost(10, TimeUnit.SECONDS).until(() ->
                !kafkaListenerEndpointRegistry.getListenerContainer("gameFinishedListener").getAssignedPartitions().isEmpty()
        );

        // Act
        kafkaTemplate.send(kafkaTopics.games(), gameFinishedEvent);

        // Assert

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            // Preparamos el captor para el argumento de tipo BigDecimal
            ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);

            // Verificamos la llamada. Para el userId usamos eq() y para el amount usamos el captor.
            verify(userServiceClient).creditUser(
                    eq(initialBet.getUserId()),
                    amountCaptor.capture() // Capturamos el BigDecimal
            );

            // Ahora, usamos AssertJ para verificar el valor capturado,
            // comparando su valor numérico sin importar la escala.
            assertThat(amountCaptor.getValue()).isEqualByComparingTo(new BigDecimal("20.00"));
        });

        // 2. Verificamos que el estado de la apuesta en la BBDD es 'WON'.
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            BetEntity settledBet = betRepository.findById(initialBet.getId()).orElseThrow();
            assertThat(settledBet.getStatus()).isEqualTo(BetStatus.WON);
        });
    }


}