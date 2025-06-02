package com.bjpractice.game_core.model;


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

//    Game game = new Game();


    @BeforeEach
    void setUp() {
        // Se ejecuta antes de cada test. Asegura que cada test comience con un mazo nuevo.
        deck = new Deck();
        deck.initializeDeck();
        player = new Player();// Aseguramos un mazo completo para la mayoría de los tests.
    }


    @Test
    void initializeDeck_ShouldCreateFullDeckWithAllCombinations() {
        Deck deckTest = new Deck();
        deckTest.initializeDeck();

        // Verificar tamaño y unicidad (sin duplicados)
        assertEquals(52, deck.getDeckListForTesting().size());
        assertEquals(52, deck.getDeckListForTesting().stream().distinct().count());

        // Verificar todas las combinaciones suit/rank
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
        // Simular estado sucio (cartas existentes)
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
        assertEquals(originalOrder.size(), shuffledOrder.size()); // Tamaño no cambia
        assertTrue(shuffledOrder.containsAll(originalOrder)); // Mismas cartas, diferente orden
    }


    // SEED STUFF

    @Test
    void shuffle_ShouldUseRandomSeed() {
        Deck deckTest = new Deck();
        deckTest.initializeDeck(); // Inicializas la baraja para tener un orden original

        // Usar semilla fija para reproducibilidad
        Collections.shuffle(deckTest.getDeckListForTesting(), new Random(123));

        List<Card> shuffledOnce = List.copyOf(deckTest.getDeckListForTesting()); // Guardas el orden después del primer shuffle

        // Reiniciar la baraja al estado original
        deckTest.initializeDeck();

        // Volver a hacer el shuffle con la misma semilla
        Collections.shuffle(deckTest.getDeckListForTesting(), new Random(123));

        // Verificar que el orden es el mismo al usar la misma semilla
        assertEquals(shuffledOnce, deckTest.getDeckListForTesting(), "Same seed should produce same order");
    }

    @Test
    void dealCard_ShouldReturnACardAndReduceDeckSize() {
        int initialSize = deck.cardsRemaining();
        Card dealtCard = deck.dealCard();

        assertNotNull(dealtCard, "The dealt card should not be null.");
        assertEquals(initialSize - 1, deck.cardsRemaining(), "Deck size should decrease by one after dealing a card.");
        // Opcional: Podrías verificar si la carta retirada ya no está en el mazo.
        assertFalse(deck.getDeckListForTesting().contains(dealtCard), "The dealt card should be removed from the deck.");
    }

    @Test
    void dealCard_ShouldDealCardsInOrderWhenNotShuffled() {
        // Si el mazo no se baraja, las cartas deberían salir en un orden predecible.
        // Basado en el orden de tus enumeraciones Suit (HEARTS primero) y Rank (TWO primero)
        Card expectedFirstCard = new Card(Card.Suit.HEARTS, Card.Rank.TWO); // <-- ¡Aquí está el cambio!

        // No es necesario llamar a deck.initializeDeck() de nuevo aquí,
        // @BeforeEach ya garantiza un mazo inicializado y ordenado para cada test.
        Card dealtCard = deck.dealCard();

        assertEquals(expectedFirstCard, dealtCard, "The first dealt card should be the expected first card of a new deck.");
        assertEquals(51, deck.cardsRemaining(), "After dealing, there should be 51 cards left.");
    }

    @Test
    void dealCard_ShouldThrowNoSuchElementExceptionWhenDeckIsEmpty() {
        // Vaciamos el mazo
        while (deck.cardsRemaining() > 0) {
            deck.dealCard();
        }

        // Verificamos que se lance la excepción correcta al intentar robar de un mazo vacío
        assertThrows(NoSuchElementException.class, () -> deck.dealCard(),
                "Should throw NoSuchElementException when trying to deal from an empty deck.");
    }

    // DEAL CARDS

    @Test
    void dealCards_ShouldDealCorrectNumberOfCardsToPlayer() {
        int cardsToDeal = 5;
        int initialDeckSize = deck.cardsRemaining();

        // --- ¡Debugging aquí! ---
        // Esto confirmará si 'player' es null ANTES de la llamada a dealCards
        System.out.println("DEBUG: Player instance before dealCards: " + player);
        if (player == null) {
            fail("Player instance is null in DeckTest before dealCards call. Check @BeforeEach.");
        }
        // --- Fin Debugging ---

        deck.dealCards(player, cardsToDeal); // La línea donde la NPE ocurría

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
    void dealCards_ShouldThrowNoSuchElementExceptionWhenNotEnoughCards() {
        for (int i = 0; i < 49; i++) {
            deck.dealCard();
        }
        assertEquals(3, deck.cardsRemaining(), "Deck should have 3 cards remaining for this test setup.");

        int cardsToDeal = 5;

        assertThrows(NoSuchElementException.class, () -> deck.dealCards(player, cardsToDeal),
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