package com.bjpractice.bets.bet;


import com.bjpractice.bets.bet.model.BetStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BetDTO {

    private UUID id;
    private Long userId;
    private UUID gameId; // Será nulo hasta que la apuesta se asocie a un juego
    private BigDecimal amount;
    private BetStatus status;
    private LocalDateTime createdAt;
}
