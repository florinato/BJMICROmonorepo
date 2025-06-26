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


    @Test
    @DisplayName ("Juego terminado, cuando se mapea a DTO, la mano del croupier es fully visible")
    void toDTO_whenGameIsOver_thenDealerHandIsFullyVisible() {


        // ARRANGE

        Card playerCard1 = new Card(Card.Suit.CLUBS, Card.Rank.TEN);
        Card playerCard2 = new Card(Card.Suit.HEARTS, Card.Rank.EIGHT);

        Card dealerCard1 = new Card(Card.Suit.SPADES, Card.Rank.FIVE);
        Card dealerCard2 = new Card(Card.Suit.DIAMONDS, Card.Rank.ACE);

        Player player = new Player();
        player.receiveCard(playerCard1);
        player.receiveCard(playerCard2);

        Dealer dealer = new Dealer();
        dealer.receiveCard(dealerCard1);
        dealer.receiveCard(dealerCard2);

        Game gameOver = new Game();
        gameOver.setPlayerForTesting(player);
        gameOver.setDealerForTesting(dealer);


        gameOver.setStateForTesting(Game.GameState.GAME_OVER);

        GameEntity gameEntity = new GameEntity(UUID.randomUUID(), 123L, UUID.randomUUID());
        gameEntity.setGameLogic(gameOver);


        // ACT

        GameDTO resultDTO = gameMapper.toDTO(gameEntity);

        // ASSSERT

        assertThat(resultDTO).isNotNull();

        assertThat(resultDTO.getDealerHand()).hasSize(2);

        assertThat(resultDTO.getDealerScore()).isEqualTo(16);


    }

    @Test
    @DisplayName("Dado un game entity nulo, cuando se mapea el DTO, entonces el resultado es null")
    void toDTO_whenEntityIsNull_thenReturnsNull() {
        // ARRANGE
        GameEntity nullEntity = null;

        // ACT
        GameDTO resultDTO = gameMapper.toDTO(nullEntity);

        // ASSERT
        assertThat(resultDTO).isNull();
    }


    @Test
    @DisplayName("Dado un GameEntity con gameLogic nulo, cuando se mapea a DTO, entonces el resultado es nulo")
    void toDTO_whenGameLogicIsNull_thenReturnsNull() {
        // ARRANGE
        GameEntity entityWithNullGame = new GameEntity(UUID.randomUUID(), 123L, UUID.randomUUID());
        entityWithNullGame.setGameLogic(null); // Forzamos que el objeto Game interno sea nulo

        // ACT
        GameDTO resultDTO = gameMapper.toDTO(entityWithNullGame);

        // ASSERT
        assertThat(resultDTO).isNull();
    }








}
