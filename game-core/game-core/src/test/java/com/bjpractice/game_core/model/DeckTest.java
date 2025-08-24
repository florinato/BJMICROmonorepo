package com.bjpractice.game_core.model;


import com.bjpractice.game_core.exception.DeckIsEmptyException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    private Deck deck;
    private Player player;




    @BeforeEach
    void setUp() {

        deck = new Deck();
        deck.initializeDeck();
        player = new Player();
    }


    @Test
    void initializeDeck_ShouldCreateFullDeckWithAllCombinations() {
        Deck deckTest = new Deck();
        deckTest.initializeDeck();


        assertEquals(52, deck.getDeckListForTesting().size());
        assertEquals(52, deck.getDeckListForTesting().stream().distinct().count());


        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                assertTrue(deck.getDeckListForTesting().contains(new Card(suit, rank)),
                        "Missing card: " + rank + " of " + suit);
            }
        }
    }

    @Test
    void initializeDeck_ShouldResetDeck() {
        Deck deckTest = new Deck();

        deckTest.getDeckListForTesting().add(new Card(Card.Suit.HEARTS, Card.Rank.ACE));

        deckTest.initializeDeck(); // Debería limpiar y recrear

        assertEquals(52, deckTest.cardsRemaining());
        assertFalse(deckTest.getDeckListForTesting().isEmpty());
    }

    @Test
    void shuffle_ShouldChangeCardOrder() {
        // Arrange
        Deck deckTest = new Deck();
        deckTest.initializeDeck();
        List<Card> originalOrder = List.copyOf(deckTest.getDeckListForTesting()); // Copia inmutable

        // Act
        deckTest.shuffle();

        // Assert
        List<Card> shuffledOrder = deckTest.getDeckListForTesting();
        assertNotEquals(originalOrder, shuffledOrder, "Deck order should change after shuffle");
        assertEquals(originalOrder.size(), shuffledOrder.size());
        assertTrue(shuffledOrder.containsAll(originalOrder));
    }


    // SEED STUFF

    @Test
    void shuffle_ShouldUseRandomSeed() {
        Deck deckTest = new Deck();
        deckTest.initializeDeck();

        // Usar semilla fija para reproducibilidad
        Collections.shuffle(deckTest.getDeckListForTesting(), new Random(123));

        List<Card> shuffledOnce = List.copyOf(deckTest.getDeckListForTesting());


        deckTest.initializeDeck();


        Collections.shuffle(deckTest.getDeckListForTesting(), new Random(123));


        assertEquals(shuffledOnce, deckTest.getDeckListForTesting(), "Same seed should produce same order");
    }

    @Test
    void dealCard_ShouldReturnACardAndReduceDeckSize() {
        int initialSize = deck.cardsRemaining();
        Card dealtCard = deck.dealCard();

        assertNotNull(dealtCard, "The dealt card should not be null.");
        assertEquals(initialSize - 1, deck.cardsRemaining(), "Deck size should decrease by one after dealing a card.");

        assertFalse(deck.getDeckListForTesting().contains(dealtCard), "The dealt card should be removed from the deck.");
    }

    @Test
    void dealCard_ShouldDealCardsInOrderWhenNotShuffled() {

        Card expectedFirstCard = new Card(Card.Suit.HEARTS, Card.Rank.TWO);


        Card dealtCard = deck.dealCard();

        assertEquals(expectedFirstCard, dealtCard, "The first dealt card should be the expected first card of a new deck.");
        assertEquals(51, deck.cardsRemaining(), "After dealing, there should be 51 cards left.");
    }

    @Test
    void dealCard_ShouldThrowDeckIsEmptyException() {

        while (deck.cardsRemaining() > 0) {
            deck.dealCard();
        }


        assertThrows(DeckIsEmptyException.class, () -> deck.dealCard(),
                "Should throw NoSuchElementException when trying to deal from an empty deck.");
    }

    // DEAL CARDS

    @Test
    void dealCards_ShouldDealCorrectNumberOfCardsToPlayer() {
        int cardsToDeal = 5;
        int initialDeckSize = deck.cardsRemaining();


        System.out.println("DEBUG: Player instance before dealCards: " + player);
        if (player == null) {
            fail("Player instance is null in DeckTest before dealCards call. Check @BeforeEach.");
        }


        deck.dealCards(player, cardsToDeal);

        assertEquals(cardsToDeal, player.getHand().size(),
                "Player should receive the specified number of cards.");
        assertEquals(initialDeckSize - cardsToDeal, deck.cardsRemaining(),
                "Deck size should decrease by the number of cards dealt.");
        player.getHand().forEach(Assertions::assertNotNull);
    }

    @Test
    void dealCards_ShouldDealZeroCardsWhenNumberOfCardsIsZero() {
        int cardsToDeal = 0;
        int initialDeckSize = deck.cardsRemaining();

        deck.dealCards(player, cardsToDeal);

        assertTrue(player.getHand().isEmpty(),
                "Player should receive no cards when numberOfCards is zero.");
        assertEquals(initialDeckSize, deck.cardsRemaining(),
                "Deck size should not change when dealing zero cards.");
    }

    @Test
    void dealCards_ShouldThrowDeckIsEmptyExceptionWhenNotEnoughCards() {
        for (int i = 0; i < 49; i++) {
            deck.dealCard();
        }
        assertEquals(3, deck.cardsRemaining(), "Deck should have 3 cards remaining for this test setup.");

        int cardsToDeal = 5;

        assertThrows(DeckIsEmptyException.class, () -> deck.dealCards(player, cardsToDeal),
                "Should throw NoSuchElementException when trying to deal more cards than available.");

        assertEquals(3, player.getHand().size(),
                "Player should have received the 3 available cards before the exception.");
        assertEquals(0, deck.cardsRemaining(),
                "Deck should be empty after attempting to deal more cards than available.");
    }

    @Test
    void dealCards_ShouldDealCorrectCardsWhenNotShuffled() {
        int cardsToDeal = 3;
        List<Card> expectedCards = List.of(
                new Card(Card.Suit.HEARTS, Card.Rank.TWO),
                new Card(Card.Suit.HEARTS, Card.Rank.THREE),
                new Card(Card.Suit.HEARTS, Card.Rank.FOUR)
        );

        deck.dealCards(player, cardsToDeal);

        assertEquals(expectedCards, player.getHand(),
                "Player should receive the first N cards in order when deck is not shuffled.");
    }

}