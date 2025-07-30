package com.bjpractice.bets.integration;

import com.bjpractice.bets.bet.model.BetEntity;
import com.bjpractice.bets.bet.model.BetStatus;
import com.bjpractice.bets.bet.repository.BetRepository;
import com.bjpractice.bets.bet.service.BetService;
import com.bjpractice.events.GameFinishedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;


class BetSettledEventIntegrationTest extends AbstractBetIntegrationTest {


    @Autowired
    private BetService betService;
    @Autowired
    private BetRepository betRepository;

    @Test
    @DisplayName("The bet balance goes into the user db")
    void whenGameIsWon_userBalanceIsUpdatedInUserService() {
        // Arrange
        long userId = 1L;
        BetEntity bet = betRepository.save(BetEntity.builder().userId(userId).amount(new BigDecimal("10.00")).status(BetStatus.PENDING_GAME).build());

        JdbcTemplate userJdbcTemplate = new JdbcTemplate(
                new DriverManagerDataSource(userDb.getJdbcUrl(), userDb.getUsername(), userDb.getPassword())
        );
        // Limpiamos primero para asegurar un estado conocido
        userJdbcTemplate.execute("DELETE FROM users WHERE id = 1");
        // Ahora insertamos nuestro usuario de prueba
        userJdbcTemplate.execute("INSERT INTO users (id, username, email, password_hash, balance, role) VALUES (1, 'testuser', 'test@test.com', 'hash', 100.00, 'USER')");

        GameFinishedEvent winEvent = new GameFinishedEvent(
                UUID.randomUUID(),
                bet.getId(),
                userId,
                "PLAYER_WINS",
                false
        );

        // Act
        betService.processGameResult(winEvent);

        //Spy stuff

//
//        System.out.println("###### Iniciando consumidor espía... ######");
//        String messageContent = consumeMessageFromTopic("bets.bet-settled-test");
//        System.out.println("###### Mensaje recibido por el espía: " + messageContent + " ######");

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Map<String, Object> userData = userJdbcTemplate.queryForMap("SELECT balance FROM users WHERE id = ?", userId);
            BigDecimal newBalance = (BigDecimal) userData.get("balance");
            assertThat(newBalance).isEqualByComparingTo(new BigDecimal("120.00"));
        });
    }


    private String consumeMessageFromTopic(String topic) {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafka.getBootstrapServers());
        props.put("group.id", "spy-consumer-" + UUID.randomUUID());
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName()); // Leemos como String para ver el contenido crudo
        props.put("auto.offset.reset", "earliest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5)); // Esperamos 5 segundos
            if (records.isEmpty()) {
                return "NO SE RECIBIÓ NINGÚN MENSAJE";
            }
            ConsumerRecord<String, String> record = records.iterator().next();
            return record.value();
        }
    }
}