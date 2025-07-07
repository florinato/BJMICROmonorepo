package com.bjpractice.bets.bet.service;


import com.bjpractice.bets.bet.model.BetEntity;
import com.bjpractice.bets.bet.model.BetStatus;
import com.bjpractice.bets.bet.repository.BetRepository;
import com.bjpractice.bets.kafka.event.GameFinishedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;


// AÑADIR DISPLAY NAMES
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BetServiceTest {

    private final BetService betService;
    private final BetRepository betRepository;

    @Autowired
    public BetServiceTest(BetService betService, BetRepository betRepository) {
        this.betService = betService;
        this.betRepository = betRepository;
    }

    @BeforeEach
    void setUp() {
        betRepository.deleteAll();
    }

    @Test
    void processGameResult_whenPlayerWins_shouldUpdateBetStatusToWon() {

        // Arrange

        BetEntity originalBet = BetEntity.builder()
                .userId(1L)
                .amount(new BigDecimal("10.00"))
                .status(BetStatus.PENDING_GAME)
                .build();
        BetEntity savedBet = betRepository.save(originalBet);
        UUID betId = savedBet.getId();

        //kafka shenanigans
        GameFinishedEvent winEvent = new GameFinishedEvent(
                UUID.randomUUID(),
                betId,
                1L,
                "PLAYER_WINS",
                false // BlackJack status
        );

        // act

        betService.processGameResult(winEvent);

        // Assert

        BetEntity updatedBet = betRepository.findById(betId).get();
        assertEquals(BetStatus.WON, updatedBet.getStatus());


    }

    @Test
    void processGameResult_whenPlayerWinsWithBlackjack_shouldUpdateBetStatusToWon() {
        // --- Arrange ---
        BetEntity originalBet = BetEntity.builder()
                .userId(1L)
                .amount(new BigDecimal("10.00"))
                .status(BetStatus.PENDING_GAME)
                .build();
        BetEntity savedBet = betRepository.save(originalBet);
        UUID betId = savedBet.getId();


        GameFinishedEvent blackjackWinEvent = new GameFinishedEvent(
                UUID.randomUUID(),
                betId,
                1L,
                "PLAYER_WINS",
                true // Again, blac kjack status is key here
        );

        // --- Act ---
        betService.processGameResult(blackjackWinEvent);

        // --- Assert ---
        BetEntity updatedBet = betRepository.findById(betId).get();
        assertEquals(BetStatus.WON, updatedBet.getStatus(), "El estado de la apuesta debería ser WON");
    }


}
