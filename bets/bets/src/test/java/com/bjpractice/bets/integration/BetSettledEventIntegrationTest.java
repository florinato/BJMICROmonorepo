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
        userJdbcTemplate.execute("INSERT INTO users (id, username, email, password_hash, balance, role) VALUES (1, 'testuser', 'test@test.com', 'hash', 100.00, 'USER')");

        GameFinishedEvent winEvent = new GameFinishedEvent(
                UUID.randomUUID(),
                bet.getId(), // Usamos el ID real de la apuesta
                userId,
                "PLAYER_WINS",
                false
        );

        // Act
        betService.processGameResult(winEvent);

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Map<String, Object> userData = userJdbcTemplate.queryForMap("SELECT balance FROM users WHERE id = ?", userId);
            BigDecimal newBalance = (BigDecimal) userData.get("balance");
            assertThat(newBalance).isEqualByComparingTo(new BigDecimal("120.00"));
        });
    }
}