package com.bjpractice.bets.bet.service;


import com.bjpractice.bets.bet.model.BetEntity;
import com.bjpractice.bets.bet.model.BetStatus;
import com.bjpractice.bets.bet.repository.BetRepository;
import com.bjpractice.bets.bet.BetDTO;
import com.bjpractice.bets.bet.BetMapper;
import com.bjpractice.bets.client.UserServiceClient;
import com.bjpractice.bets.exception.InvalidBetAmountException;
import com.bjpractice.bets.kafka.event.GameFinishedEvent;
import com.bjpractice.bets.kafka.event.PlayerDoubleEvent;
import com.bjpractice.bets.kafka.listener.GameEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


// UNDER CONSTRUCTION !!!!


@Service
public class BetService {

    private final BetRepository betRepository;
    private final BetMapper betMapper;
    private final UserServiceClient userServiceClient;
    private static final Logger log = LoggerFactory.getLogger(BetService.class);

    public BetService(BetRepository betRepository, BetMapper betMapper, UserServiceClient userServiceClient) {
        this.betRepository = betRepository;
        this.betMapper = betMapper;
        this.userServiceClient = userServiceClient;
    }


    public BetDTO placeBet(Long userId, BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBetAmountException("El monto de la apuesta debe ser positivo.");
        }


        BetEntity newBet = BetEntity.builder()
                .userId(userId)
                .amount(amount)
                .build();

        BetEntity savedBet = betRepository.save(newBet);

        return betMapper.toDTO(savedBet);
    }


    public void processGameResult(GameFinishedEvent event) {
        BetEntity bet = betRepository.findById(event.betId())
                .orElseThrow(() -> new RuntimeException("Bet not found for id " + event.betId()));

        BigDecimal totalReturned = BigDecimal.ZERO;

        switch (event.result()) {

            case "PLAYER_WINS" -> {
                bet.setStatus(BetStatus.WON);

                BigDecimal payout;
                if (event.playerHasBlackjack()) {
                    payout = bet.getAmount().multiply(new BigDecimal("1.5"));
                } else {
                    payout = bet.getAmount();
                }

                totalReturned = bet.getAmount().add(payout);

                log.info("PAYOUT para userId {}: Devolviendo {} (apuesta de {} + ganancia de {})",
                        bet.getUserId(), totalReturned, bet.getAmount(), payout);


            }
            case "DEALER_WINS" -> bet.setStatus(BetStatus.LOST);
            case "PUSH" -> {
                bet.setStatus(BetStatus.PUSH);
                totalReturned = bet.getAmount();

            }
            default -> throw new IllegalArgumentException("Resultado del juego desconocido " + event.result());
        }

        betRepository.save(bet);
        log.info("Bet{} actualizada a estado {}", bet.getId(), bet.getStatus());

        if (totalReturned.compareTo(BigDecimal.ZERO) > 0) {
            log.info("PAYOUT/RETURN para userId {}: Devolviendo un total de {}", bet.getUserId(), totalReturned);
            userServiceClient.creditUser(bet.getUserId(), totalReturned);

        }


    }



    public void processPlayerDouble(PlayerDoubleEvent event) {
        // Buscamos la apuesta usando el betId que viene en el evento
        BetEntity bet = betRepository.findById(event.betId())
                .orElseThrow(() -> new RuntimeException("Bet not found for id " + event.betId()));

        // Calculamos el nuevo monto
        BigDecimal originalAmount = bet.getAmount();
        BigDecimal newAmount = originalAmount.multiply(BigDecimal.valueOf(2));

        // Actualizamos la entidad
        bet.setAmount(newAmount);
        betRepository.save(bet);

        log.info("Apuesta {} (userId: {}) doblada. Nuevo monto: {}", bet.getId(), bet.getUserId(), newAmount);
    }

}
