package com.bjpractice.game_core.model;


import org.junit.jupiter.api.Test;


import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    Game game = new Game();


    @Test
    void initializeDeck_ShouldCreateFullDeckWithAllCombinations() {
        Deck deck = new Deck();
        deck.initializeDeck();

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
        Deck deck = new Deck();
        // Simular estado sucio (cartas existentes)
        deck.getDeckListForTesting().add(new Card(Card.Suit.HEARTS, Card.Rank.ACE));

        deck.initializeDeck(); // Debería limpiar y recrear

        assertEquals(52, deck.cardsRemaining());
        assertFalse(deck.getDeckListForTesting().isEmpty());
    }

    @Test
    void shuffle_ShouldChangeCardOrder() {
        // Arrange
        Deck deck = new Deck();
        deck.initializeDeck();
        List<Card> originalOrder = List.copyOf(deck.getDeckListForTesting()); // Copia inmutable

        // Act
        deck.shuffle();

        // Assert
        List<Card> shuffledOrder = deck.getDeckListForTesting();
        assertNotEquals(originalOrder, shuffledOrder, "Deck order should change after shuffle");
        assertEquals(originalOrder.size(), shuffledOrder.size()); // Tamaño no cambia
        assertTrue(shuffledOrder.containsAll(originalOrder)); // Mismas cartas, diferente orden
    }


    // SEED STUFF

    @Test
    void shuffle_ShouldUseRandomSeed() {
        Deck deck = new Deck();
        deck.initializeDeck(); // Inicializas la baraja para tener un orden original

        // Usar semilla fija para reproducibilidad
        Collections.shuffle(deck.getDeckListForTesting(), new Random(123));

        List<Card> shuffledOnce = List.copyOf(deck.getDeckListForTesting()); // Guardas el orden después del primer shuffle

        // Reiniciar la baraja al estado original
        deck.initializeDeck();

        // Volver a hacer el shuffle con la misma semilla
        Collections.shuffle(deck.getDeckListForTesting(), new Random(123));

        // Verificar que el orden es el mismo al usar la misma semilla
        assertEquals(shuffledOnce, deck.getDeckListForTesting(), "Same seed should produce same order");
    }

}