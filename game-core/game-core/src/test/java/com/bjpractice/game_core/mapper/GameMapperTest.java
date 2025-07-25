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

        // ✅ CORRECCIÓN: Nombramos las variables para que reflejen la lógica real del Blackjack
        Card dealerCard1_hidden = new Card(Card.Suit.SPADES, Card.Rank.QUEEN);
        Card dealerCard2_visible = new Card(Card.Suit.CLUBS, Card.Rank.SEVEN);

        GameEntity gameEntity = GameEntityTestBuilder.createGameInProgressWithSpecificCards(
                userId,
                betId,
                List.of(
                        new Card(Card.Suit.HEARTS, Card.Rank.TEN),
                        new Card(Card.Suit.SPADES, Card.Rank.FIVE)
                ),
                // El orden importa: la oculta primero, la visible después
                List.of(
                        dealerCard1_hidden,
                        dealerCard2_visible
                )
        );

        // 2. Act
        GameDTO resultDTO = gameMapper.toDTO(gameEntity);

        // 3. Assert
        assertThat(resultDTO).isNotNull();
        assertThat(resultDTO.getDealerHand()).hasSize(1); // Solo se debe ver una carta

        // ✅ CORRECCIÓN: Verificamos que la carta visible es la SEGUNDA
        CardDTO visibleDealerCardDTO = resultDTO.getDealerHand().get(0);
        assertThat(visibleDealerCardDTO.getRank()).isEqualTo(dealerCard2_visible.getRank().name());
        assertThat(visibleDealerCardDTO.getSuit()).isEqualTo(dealerCard2_visible.getSuit().name());

        // Verificamos que la puntuación visible es la de la SEGUNDA carta
        assertThat(resultDTO.getDealerScore()).isEqualTo(dealerCard2_visible.getValue());
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
