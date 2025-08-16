package com.bjpractice.user.integration;

import com.bjpractice.events.BetSettledEvent;
import com.bjpractice.user.config.properties.KafkaTopics;
import com.bjpractice.user.entity.User;
import com.bjpractice.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;


@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserEventListenerTest extends AbstractIntegrationTest{

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KafkaTopics kafkaTopics;


    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void givenUserExists_whenBetSettledEventIsConsumed_thenUserBalanceIsUpdated() {
        // Arrange

        User testUser = User.builder()
                .username("testUser")
                .email("test@example.com")
                .passwordHash("somehash")
                .balance(new BigDecimal("100.00"))
                .build();
        userRepository.save(testUser);


        BigDecimal amountWon = new BigDecimal("50.50");
        BetSettledEvent event = new BetSettledEvent(testUser.getId(), amountWon);

        // Act

        kafkaTemplate.send(kafkaTopics.betSettled(), event);

        // Assert
        // Usamos Awaitility para esperar de forma asíncrona hasta que la aserción sea verdadera.
        // Esto es crucial porque el listener de Kafka procesa el mensaje en un hilo separado.
        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {

                    User updatedUser = userRepository.findById(testUser.getId()).orElseThrow(
                            () -> new AssertionError("El usuario no fue encontrado en la base de datos")
                    );


                    BigDecimal expectedBalance = new BigDecimal("150.50");
                    assertThat(updatedUser.getBalance()).isEqualByComparingTo(expectedBalance);
                });
    }
}
