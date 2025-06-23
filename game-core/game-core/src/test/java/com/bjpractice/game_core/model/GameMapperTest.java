package com.bjpractice.game_core.model;

import com.bjpractice.game_core.dto.CardDTO;
import com.bjpractice.game_core.dto.GameDTO;
import com.bjpractice.game_core.mapper.GameMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


class GameMapperTest {

    private GameMapper gameMapper;

    @BeforeEach
    void setUp() {

        gameMapper = new GameMapper();
    }

    @Test
    @DisplayName("Se oculta la primera carta del croupier en el mapper")
    void toDTO_whenGameIsInProgress_thenDealerHandIsMasked() {

        // 1. Arrange:
        // Esta lógica ahora funciona porque la clase de test está en el paquete 'model'
        Card playerCard1 = new Card(Card.Suit.HEARTS, Card.Rank.TEN);
        Card playerCard2 = new Card(Card.Suit.SPADES, Card.Rank.FIVE);
        Card dealerCard1_hidden = new Card(Card.Suit.CLUBS, Card.Rank.SEVEN);
        Card dealerCard2_visible = new Card(Card.Suit.SPADES, Card.Rank.QUEEN);

        Player player = new Player();
        player.receiveCard(playerCard1);
        player.receiveCard(playerCard2);

        Dealer dealer = new Dealer();
        dealer.receiveCard(dealerCard1_hidden);
        dealer.receiveCard(dealerCard2_visible);

        Game gameInProgress = new Game();
        gameInProgress.setPlayerForTesting(player); // OK
        gameInProgress.setDealerForTesting(dealer); // OK
        gameInProgress.setStateForTesting(Game.GameState.PLAYER_TURN); // OK

        GameEntity gameEntity = new GameEntity(UUID.randomUUID(), 123L, UUID.randomUUID());
        gameEntity.setGameLogic(gameInProgress);

        // 2. Act
        GameDTO resultDTO = gameMapper.toDTO(gameEntity);

        // 3. Assert
        assertThat(resultDTO).isNotNull();
        assertThat(resultDTO.getDealerHand()).hasSize(1);

        CardDTO visibleDealerCardDTO = resultDTO.getDealerHand().get(0);
        assertThat(visibleDealerCardDTO.getRank()).isEqualTo(dealerCard2_visible.getRank().name());
        assertThat(visibleDealerCardDTO.getSuit()).isEqualTo(dealerCard2_visible.getSuit().name());

        assertThat(resultDTO.getDealerScore()).isEqualTo(dealerCard2_visible.getValue());
    }



}
