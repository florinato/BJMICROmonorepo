package com.bjpractice.game_core.dto;

import com.bjpractice.game_core.model.Game;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;


// PRELIMINAR / UNDER CONSTRUCTION
@Data
public class GameDTO {
    private String gameId;
    private String playerId;
    private BigDecimal currentBet;
    private Game.GameState state;
    private Game.GameResult result;
    private List<CardDTO> playerHand;
    private List<CardDTO> dealerHand;
//    private boolean canHit; Mirar esto
//    private boolean canStand;
//    private boolean canDouble;
    private Instant createdAt;
    // Getters y setters
}