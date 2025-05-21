package com.bjpractice.game_core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerTest {

    @Test
    void testCalculateHandValue_NoAces() {
        Player player = new Player();
        player.receiveCard(new Card(Card.Suit.HEARTS, Card.Rank.TEN));
        player.receiveCard(new Card(Card.Suit.CLUBS, Card.Rank.SEVEN));
        assertEquals(17, player.calculateHandValue());
    }
}
