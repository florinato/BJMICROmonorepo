package com.bjpractice.game_core.mapper;

import com.bjpractice.game_core.dto.CardDTO;
import com.bjpractice.game_core.dto.GameDTO;
import com.bjpractice.game_core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
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

        Long userId = 123L;
        UUID betId = UUID.randomUUID();

        Card dealerCard1_visible = new Card(Card.Suit.CLUBS, Card.Rank.SEVEN);
        Card dealerCard2_hidden = new Card(Card.Suit.SPADES, Card.Rank.QUEEN);

        GameEntity gameEntity = GameEntityTestBuilder.createGameInProgressWithSpecificCards(
                userId,
                betId,
                List.of(
                        new Card(Card.Suit.HEARTS, Card.Rank.TEN),
                        new Card(Card.Suit.SPADES, Card.Rank.FIVE)
                ),
                List.of(
                        dealerCard1_visible,
                        dealerCard2_hidden
                )

        );

        GameDTO resultDTO = gameMapper.toDTO(gameEntity);

        assertThat(resultDTO).isNotNull();
        assertThat(resultDTO.getDealerHand()).hasSize(1);

        CardDTO visibleDealerCardDTO = resultDTO.getDealerHand().get(0);
        assertThat(visibleDealerCardDTO.getRank()).isEqualTo(dealerCard1_visible.getRank().name());
        assertThat(visibleDealerCardDTO.getSuit()).isEqualTo(dealerCard1_visible.getSuit().name());

        assertThat(resultDTO.getDealerScore()).isEqualTo(dealerCard1_visible.getValue());


    }


    @Test
    @DisplayName("Juego terminado, cuando se mapea a DTO, la mano del croupier es fully visible")
    void toDTO_whenGameIsOver_thenDealerHandIsFullyVisible() {

        Long userId = 123L;
        UUID betId = UUID.randomUUID();

        List<Card> playerCards = List.of(
                new Card(Card.Suit.CLUBS, Card.Rank.TEN),
                new Card(Card.Suit.HEARTS, Card.Rank.EIGHT)
        );

        List<Card> dealerCards = List.of(
                new Card(Card.Suit.SPADES, Card.Rank.FIVE),
                new Card(Card.Suit.DIAMONDS, Card.Rank.ACE)
        );


        GameEntity gameEntity = GameEntityTestBuilder.createFinishedGameWithSpecificCards(
                userId, betId, playerCards, dealerCards
        );


        GameDTO resultDTO = gameMapper.toDTO(gameEntity);


        assertThat(resultDTO).isNotNull();
        assertThat(resultDTO.getDealerHand()).hasSize(2); // Se ven las dos cartas
        assertThat(resultDTO.getDealerScore()).isEqualTo(16); // Se ve la puntuación real
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
