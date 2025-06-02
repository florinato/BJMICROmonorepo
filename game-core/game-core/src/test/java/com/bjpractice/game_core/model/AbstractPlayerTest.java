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



}

