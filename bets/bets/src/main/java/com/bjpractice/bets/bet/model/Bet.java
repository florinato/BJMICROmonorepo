package com.bjpractice.bets.bet.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Bet {



        private UUID id;
        private Long userId; // Está por ver como se construirá user
        private UUID gameId;
        private BigDecimal amount;
        private BetStatus status;
        private LocalDateTime createdAt;



}
