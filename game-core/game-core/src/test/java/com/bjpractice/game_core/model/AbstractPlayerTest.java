package com.bjpractice.game_core.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class AbstractPlayerTest {


    private Player player;
    private Card ten;
    private Card seven;
    private Card ace;
    private Card king;

    @BeforeEach
    void setUp() {
        player = new Player();
        ten = new Card(Card.Suit.HEARTS, Card.Rank.TEN);
        seven = new Card(Card.Suit.CLUBS, Card.Rank.SEVEN);
        ace = new Card(Card.Suit.SPADES, Card.Rank.ACE);
        king = new Card(Card.Suit.DIAMONDS, Card.Rank.KING);
    }


    @Test
    void calculateHandValue_noAces_returnsCorrectValue() {
        player.receiveCard(ten);
        player.receiveCard(seven);
        assertEquals(17, player.calculateHandValue());
    }

    @Test
    void calculateHandValue_withAceConversion_returnsAdjustedValue() {
        player.receiveCard(ace);
        player.receiveCard(ten);
        player.receiveCard(seven);
        assertEquals(18, player.calculateHandValue());
    }


    @Test
    void isBust_handValueUnder21_returnsFalse() {
        player.receiveCard(ten);
        player.receiveCard(seven);
        assertFalse(player.isBust());
    }

    @Test
    void isBust_handValueOver21NoAces_returnsTrue() {
        player.receiveCard(ten);
        player.receiveCard(seven);
        player.receiveCard(king);
        assertTrue(player.isBust());
    }

    @Test
    void isBust_handValueOver21WithAceConversion_returnsFalse() {
        player.receiveCard(ace);
        player.receiveCard(ten);
        player.receiveCard(seven);
        assertFalse(player.isBust());
    }

    @Test
    void isBust_handValueExactly21_returnsFalse() {
        player.receiveCard(ace);
        player.receiveCard(king);
        assertFalse(player.isBust());
    }


    @Test
    void hasBlackjack_withAceAndTenValueCard_returnsTrue() {
        player.receiveCard(ace);
        player.receiveCard(king);  // ACE + KING (10) = 21
        assertTrue(player.hasBlackjack());
    }

    @Test
    void hasBlackjack_withTwoNonAceCardsSumming21_returnsFalse() {
        player.receiveCard(king);
        player.receiveCard(ten);
        player.receiveCard(ace);  // KING + TEN + ACE = 21, pero son 3 cartas
        assertFalse(player.hasBlackjack());
    }

    @Test
    void hasBlackjack_withAceAndNonTenValueCard_returnsFalse() {
        player.receiveCard(ace);
        player.receiveCard(seven);  // ACE + SEVEN = 18 (no blackjack)
        assertFalse(player.hasBlackjack());
    }

    @Test
    void hasBlackjack_withNonAceCardsSumming21_returnsFalse() {
        player.receiveCard(king);
        player.receiveCard(seven);
        player.receiveCard(new Card(Card.Suit.HEARTS, Card.Rank.FOUR));  // 10 + 7 + 4 = 21, pero son 3 cartas
        assertFalse(player.hasBlackjack());
    }

    @Test
    void hasBlackjack_withLessThanTwoCards_returnsFalse() {
        player.receiveCard(ace);  // Solo 1 carta
        assertFalse(player.hasBlackjack());
    }

    @Test
    void clearHand_removesAllCardsFromHand() {
        // Arrange: Añadimos cartas
        player.receiveCard(ace);
        player.receiveCard(king);
        assertEquals(2, player.getHand().size());  // Verificación previa

        // Act: Limpiamos la mano
        player.clearHand();

        // Assert: Mano vacía
        assertTrue(player.getHand().isEmpty());
        assertEquals(0, player.calculateHandValue());  // Valor debería ser 0
    }


}

