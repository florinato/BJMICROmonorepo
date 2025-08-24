package com.bjpractice.bets.bet.service;


import com.bjpractice.bets.bet.model.BetEntity;
import com.bjpractice.bets.bet.model.BetStatus;
import com.bjpractice.bets.bet.repository.BetRepository;
import com.bjpractice.bets.client.UserServiceClient;
import com.bjpractice.bets.config.TestUserClientConfiguration;
import com.bjpractice.bets.kafka.event.PlayerDoubleEvent;
import com.bjpractice.events.GameFinishedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import com.bjpractice.bets.kafka.listener.GameEventListener;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;


// AÑADIR DISPLAY NAMES
@SpringBootTest
@Import(TestUserClientConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BetServiceTest {

    private final BetService betService;
    private final BetRepository betRepository;
    private final UserServiceClient userServiceClient;
    private final GameEventListener gameEventListener;

    @Autowired
    public BetServiceTest(BetService betService, BetRepository betRepository, UserServiceClient userServiceClient, GameEventListener gameEventListener) {
        this.betService = betService;
        this.betRepository = betRepository;
        this.userServiceClient = userServiceClient;
        this.gameEventListener = gameEventListener;
    }

    @BeforeEach
    void setUp() {

        Mockito.reset(userServiceClient);
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

    @Test
    @DisplayName("When player wins the method should cash the proper amount")
    void processGameResult_whenPlayerWins_shouldUpdateStatusAndCreditCorrectPayout() {

        // Arrange

        BetEntity originalBet = BetEntity.builder()
                .userId(1L)
                .amount(new BigDecimal("10.00"))
                .status(BetStatus.PENDING_GAME)
                .build();
        BetEntity savedBet = betRepository.save(originalBet);
        UUID betId = savedBet.getId();

        GameFinishedEvent winEvent = new GameFinishedEvent(
                UUID.randomUUID(), betId, 1L, "PLAYER_WINS", false
        );

        BigDecimal expectedCredit = new BigDecimal("20.00");

        // Act

        betService.processGameResult(winEvent);

        // Assert

        BetEntity updatedBet = betRepository.findById(betId).get();
        assertEquals(BetStatus.WON, updatedBet.getStatus());

        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);

        verify(userServiceClient).creditUser(userIdCaptor.capture(), amountCaptor.capture());

        assertEquals(1L, userIdCaptor.getValue());
        assertEquals(0, expectedCredit.compareTo(amountCaptor.getValue()), "El monto acreditado debe ser 20.00");


    }

    @Test
    @DisplayName("When player wins with Blackjack, should update status and credit 1.5x payout")
    void processGameResult_whenPlayerWinsWithBlackjack_shouldCreditCorrectPayout() {

        BetEntity originalBet = BetEntity.builder()
                .userId(1L)
                .amount(new BigDecimal("10.00"))
                .status(BetStatus.PENDING_GAME)
                .build();
        BetEntity savedBet = betRepository.save(originalBet);
        UUID betId = savedBet.getId();


        GameFinishedEvent blackjackWinEvent = new GameFinishedEvent(
                UUID.randomUUID(),betId,1L,"PLAYER_WINS",true);

        BigDecimal expectedCredit = new BigDecimal("25.00");

        betService.processGameResult(blackjackWinEvent);

        BetEntity updatedBet = betRepository.findById(betId).get();
        assertEquals(BetStatus.WON, updatedBet.getStatus());

        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);

        verify(userServiceClient).creditUser(userIdCaptor.capture(), amountCaptor.capture());

        assertEquals(1L, userIdCaptor.getValue());
        assertEquals(0, expectedCredit.compareTo(amountCaptor.getValue()), "El monto acreditado debe ser 25.00 por el Blackjack");


    }

    @Test
    @DisplayName("When dealer wins, should update status to LOST and not credit user")
    void processGameResult_whenDealerWins_shouldUpdateStatusToLost() {
        // --- Arrange ---
        BetEntity originalBet = BetEntity.builder()
                .userId(1L)
                .amount(new BigDecimal("10.00"))
                .status(BetStatus.PENDING_GAME)
                .build();
        BetEntity savedBet = betRepository.save(originalBet);
        UUID betId = savedBet.getId();


        GameFinishedEvent dealerWinEvent = new GameFinishedEvent(
                UUID.randomUUID(), betId, 1L, "DEALER_WINS", false
        );

        // --- Act ---
        betService.processGameResult(dealerWinEvent);

        // --- Assert ---

        BetEntity updatedBet = betRepository.findById(betId).get();
        assertEquals(BetStatus.LOST, updatedBet.getStatus());



        verify(userServiceClient, Mockito.never()).creditUser(anyLong(), any(BigDecimal.class));
    }


    @Test
    @DisplayName("When push, should update status to PUSH and return original bet amount")
    void processGameResult_whenPush_shouldReturnOriginalBetAmount() {
        // --- Arrange ---
        BetEntity originalBet = BetEntity.builder()
                .userId(1L)
                .amount(new BigDecimal("10.00"))
                .status(BetStatus.PENDING_GAME)
                .build();
        BetEntity savedBet = betRepository.save(originalBet);
        UUID betId = savedBet.getId();


        GameFinishedEvent pushEvent = new GameFinishedEvent(
                UUID.randomUUID(), betId, 1L, "PUSH", false
        );


        BigDecimal expectedCredit = new BigDecimal("10.00");

        // --- Act ---
        betService.processGameResult(pushEvent);

        // --- Assert ---

        BetEntity updatedBet = betRepository.findById(betId).get();
        assertEquals(BetStatus.PUSH, updatedBet.getStatus());


        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);

        verify(userServiceClient).creditUser(userIdCaptor.capture(), amountCaptor.capture());

        assertEquals(1L, userIdCaptor.getValue());
        assertEquals(0, expectedCredit.compareTo(amountCaptor.getValue()), "Se debe devolver la apuesta original de 10.00");
    }


    @Test
    @DisplayName("When PlayerDoubleEvent is received, should double the bet amount")
    void handlePlayerDouble_shouldDoubleBetAmount() {
        // --- Arrange ---

        BetEntity originalBet = BetEntity.builder()
                .userId(1L)
                .amount(new BigDecimal("10.00"))
                .status(BetStatus.PENDING_GAME)
                .build();
        BetEntity savedBet = betRepository.save(originalBet);
        UUID betId = savedBet.getId();


        PlayerDoubleEvent doubleEvent = new PlayerDoubleEvent(
                UUID.randomUUID(),
                betId,
                1L
        );


        BigDecimal expectedAmount = new BigDecimal("20.00");

        // --- Act ---

        gameEventListener.handlePlayerDouble(doubleEvent);

        // --- Assert ---

        BetEntity updatedBet = betRepository.findById(betId).get();
        assertEquals(0, expectedAmount.compareTo(updatedBet.getAmount()), "El monto de la apuesta debería haberse duplicado a 20.00");
    }




}
