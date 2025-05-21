package com.bjpractice.game_core.dto;
import com.bjpractice.game_core.model.Card;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


// PRELIMINAR

@Getter
@RequiredArgsConstructor
public class CardDTO {
    public enum SuitDTO {
        HEARTS, DIAMONDS, CLUBS, SPADES
    }

    public enum RankDTO {
        TWO, THREE, FOUR, FIVE, SIX,
        SEVEN, EIGHT, NINE, TEN,
        JACK, QUEEN, KING, ACE
    }

    private final SuitDTO suit;
    private final RankDTO rank;
    private final int value; // Valor numérico de la carta
    private final boolean isAce;

    // Método factory para crear desde la entidad Card
    public static CardDTO fromEntity(Card card) {
        return new CardDTO(
                SuitDTO.valueOf(card.getSuit().name()),
                RankDTO.valueOf(card.getRank().name()),
                card.getValue(),
                card.isAce()
        );
    }

    @Override
    public String toString() {
        return rank + "_OF_" + suit;
    }
}