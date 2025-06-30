package com.bjpractice.game_core.dto;

import com.bjpractice.game_core.model.Game;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class GameDTO {

    private UUID gameId;
    private Long userId;
    private Game.GameState gameState;
    private Game.GameResult gameResult;

    private List<CardDTO> playerHand;
    private int playerScore;

    private List<CardDTO> dealerHand;
    private int dealerScore;

}
