package com.bjpractice.game_core.model;


import com.bjpractice.game_core.exception.InvalidGameActionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class GameTest {


    private Game game;
    private Deck mockDeck;
    private Player mockPlayer;
    private Dealer mockDealer;

    @BeforeEach
    void setUp() {

        game = new Game();


        mockDeck = mock(Deck.class);
        mockDealer = mock(Dealer.class);
        mockPlayer = mock(Player.class);


        game.setPlayerForTesting(mockPlayer);
        game.setDealerForTesting(mockDealer);
        game.setDeckForTesting(mockDeck);
        game.setStateForTesting(Game.GameState.WAITING_TO_START);
    }


    // START GAME

    @Test
    void startGame_ShouldInitializeGame_WhenInWaitingState() {


        doNothing().when(mockDeck).dealCards(any(AbstractPlayer.class), anyInt());


        game.startGame();


        verify(mockDeck).initializeDeck();
        verify(mockDeck).shuffle();
        verify(mockDeck).dealCards(any(Player.class), eq(2)); // Usamos any() y eq()
        verify(mockDeck).dealCards(any(Dealer.class), eq(2));
        assertEquals(Game.GameState.PLAYER_TURN, game.getState());
    }


    @Test
    void startGame_InitializesGame_WhenInWaitingState() {

        mockDeck = mock(Deck.class);
        game = new Game();
        game.setDeckForTesting(mockDeck);


        doNothing().when(mockDeck).dealCards(any(AbstractPlayer.class), anyInt());


        game.startGame();


        verify(mockDeck).initializeDeck();
        verify(mockDeck).shuffle();
        verify(mockDeck).dealCards(game.getPlayer(), 2);
        verify(mockDeck).dealCards(game.getDealer(), 2);
        assertEquals(Game.GameState.PLAYER_TURN, game.getState());
    }


    // PLAY DEALER HAND

    @Test
    void playDealerHand_ShouldStopAfterBust() {

        when(mockDealer.calculateHandValue())
                .thenReturn(16)
                .thenReturn(22); // Bust

        when(mockDeck.dealCard())
                .thenReturn(new Card(Card.Suit.SPADES, Card.Rank.KING));


        game.playDealerHand();


        verify(mockDeck, times(1)).dealCard(); // Solo 1 carta antes del bust
    }


    @Test
    void playDealerHand_ShouldDrawCardsUntil17OrHigher() {

        when(mockDealer.calculateHandValue())
                .thenReturn(16)
                .thenReturn(17);

        Card mockCard = new Card(Card.Suit.HEARTS, Card.Rank.ACE);
        when(mockDeck.dealCard()).thenReturn(mockCard);


        game.playDealerHand();


        verify(mockDealer, times(2)).calculateHandValue();
        verify(mockDeck, times(1)).dealCard();
        verify(mockDealer, times(1)).receiveCard(mockCard);
    }

    @Test
    void playDealerHand_ShouldNotDrawWhenHandIs17OrHigher() {

        when(mockDealer.calculateHandValue()).thenReturn(17);


        game.playDealerHand();


        verify(mockDealer, atLeastOnce()).calculateHandValue();
        verify(mockDeck, never()).dealCard();
    }


    // DETERMINE WINNER

    @Test
    void determineWinner_PlayerBust_DealerWins() {

        when(mockPlayer.isBust()).thenReturn(true);


        Game.GameResult result = game.determineWinner();


        assertEquals(Game.GameResult.DEALER_WINS, result);
    }

    @Test
    void determineWinner_DealerBust_PlayerWins() {

        when(mockDealer.isBust()).thenReturn(true);


        Game.GameResult result = game.determineWinner();


        assertEquals(Game.GameResult.PLAYER_WINS, result);
    }

    @Test
    void determineWinner_PlayerHigherValue_PlayerWins() {

        when(mockPlayer.calculateHandValue()).thenReturn(20);
        when(mockDealer.calculateHandValue()).thenReturn(18);


        Game.GameResult result = game.determineWinner();


        assertEquals(Game.GameResult.PLAYER_WINS, result);
    }

    @Test
    void determineWinner_DealerHigherValue_DealerWins() {

        when(mockPlayer.calculateHandValue()).thenReturn(18);
        when(mockDealer.calculateHandValue()).thenReturn(20);


        Game.GameResult result = game.determineWinner();


        assertEquals(Game.GameResult.DEALER_WINS, result);
    }

    @Test
    void determineWinner_EqualValue_Push() {

        when(mockPlayer.calculateHandValue()).thenReturn(18);
        when(mockDealer.calculateHandValue()).thenReturn(18);


        Game.GameResult result = game.determineWinner();


        assertEquals(Game.GameResult.PUSH, result);
    }


    // IS GAME OVER


    @Test
    void isGameOver_ShouldReturnTrue_WhenGameStateIsOver() {

        game.setStateForTesting(Game.GameState.GAME_OVER);

        boolean result = game.isGameOver();

        assertTrue(result);

    }

    @Test
    void isGameOver_ShouldReturnFalse_WhenGameStateIsNotOver() {

        game.setStateForTesting(Game.GameState.PLAYER_TURN);

        boolean result = game.isGameOver();

        assertFalse(result);
    }


    // HIT

    @Test
    void playerHit_ShouldAddCard_WhenPlayerTurn() {

        game.setStateForTesting(Game.GameState.PLAYER_TURN);
        Card mockCard = new Card(Card.Suit.HEARTS, Card.Rank.QUEEN);
        when(mockDeck.dealCard()).thenReturn(mockCard);
        when(mockPlayer.isBust()).thenReturn(false);


        game.playerHit();


        verify(mockPlayer).receiveCard(mockCard);
        assertEquals(Game.GameState.PLAYER_TURN, game.getState()); // Estado no cambia
    }

    @Test
    void playerHit_ShouldEndGame_WhenPlayerBusts() {

        game.setStateForTesting(Game.GameState.PLAYER_TURN);
        Card mockCard = new Card(Card.Suit.SPADES, Card.Rank.KING);
        when(mockDeck.dealCard()).thenReturn(mockCard);
        when(mockPlayer.isBust()).thenReturn(true);


        game.playerHit();


        verify(mockPlayer).receiveCard(mockCard);
        assertEquals(Game.GameResult.DEALER_WINS, game.getResult());
        assertEquals(Game.GameState.GAME_OVER, game.getState());
    }

    @Test
    @DisplayName("Should throw exception for 'hit' when is not the player's turn")
    void playerHit_ShouldThrowException_WhenNotPlayerTurn() {

        game.setStateForTesting(Game.GameState.DEALER_TURN); // Estado incorrecto


        assertThrows(InvalidGameActionException.class, () -> {
            game.playerHit();
        });


        verifyNoInteractions(mockDeck);
        verifyNoInteractions(mockPlayer);

    }


    // STAND

    @Test
    void playerStand_ShouldChangeToDealerTurn_WhenPlayerTurn() {

        game.setStateForTesting(Game.GameState.PLAYER_TURN);
        when(mockDealer.calculateHandValue()).thenReturn(17); // Dealer no roba más
        when(mockDeck.dealCard()).thenReturn(new Card(Card.Suit.HEARTS, Card.Rank.FIVE));
        when(mockPlayer.calculateHandValue()).thenReturn(18);
        when(mockDealer.calculateHandValue()).thenReturn(17); // Final del dealer


        game.playerStand();


        assertEquals(Game.GameState.GAME_OVER, game.getState());
        assertNotNull(game.getResult());
    }

    @Test
    @DisplayName("Should throw an exception when is not the player turn")
    void playerStand_ShouldThrowException_WhenNotPlayerTurn() {

        game.setStateForTesting(Game.GameState.DEALER_TURN);

        assertThrows(InvalidGameActionException.class, () -> {
            game.playerStand();
        });

        // Esta parte nos sirve para que no se ejecute la parte del metodo (Por ej. determine winner) que no es relevante para este test
        verifyNoInteractions(mockDealer);
        verifyNoInteractions(mockDeck);


    }

    // DOUBLE

    @Test
    void playerDouble_ShouldAddOneCardAndEndGame_WhenValid() {

        game.setStateForTesting(Game.GameState.PLAYER_TURN);

        // Crear el mock para la mano del jugador
        List<Card> mockHand = mock(List.class);

        // Configurar el comportamiento del mock para que devuelva el tamaño esperado
        when(mockHand.size()).thenReturn(2);

        // Configurar el mock de getHand() para que devuelva el mock de la mano
        when(mockPlayer.getHand()).thenReturn(mockHand);

        // Configurar el comportamiento del mazo y del jugador
        when(mockDeck.dealCard()).thenReturn(new Card(Card.Suit.DIAMONDS, Card.Rank.FIVE));
        when(mockPlayer.isBust()).thenReturn(false);
        when(mockPlayer.calculateHandValue()).thenReturn(19);

        // Simular el turno del dealer
        when(mockDealer.calculateHandValue())
                .thenReturn(15)
                .thenReturn(17);

        // Ejecutar la acción de doblar
        game.playerDouble();

        // Verificaciones
        verify(mockPlayer).receiveCard(any(Card.class));  // El jugador recibe una carta
        verify(mockDealer, atLeastOnce()).calculateHandValue();  // El dealer calcula el valor de la mano
        verify(mockDealer, atLeastOnce()).receiveCard(any(Card.class)); // El dealer recibe una carta

        // Verificar que el resultado y estado son los esperados
        assertEquals(Game.GameResult.PLAYER_WINS, game.getResult());
        assertEquals(Game.GameState.GAME_OVER, game.getState());
    }


    @Test
    void playerDouble_ShouldDeclareDealerWin_WhenPlayerBusts() {

        game.setStateForTesting(Game.GameState.PLAYER_TURN);


        List<Card> mockHand = mock(List.class);
        when(mockHand.size()).thenReturn(2);
        when(mockPlayer.getHand()).thenReturn(mockHand);
        when(mockDeck.dealCard()).thenReturn(new Card(Card.Suit.HEARTS, Card.Rank.KING));
        when(mockPlayer.isBust()).thenReturn(true);


        game.playerDouble();


        verify(mockPlayer).receiveCard(any(Card.class));
        verify(mockDealer, never()).receiveCard(any()); // dealer no juega
        verify(mockDealer, never()).calculateHandValue(); // ni calcula

        assertEquals(Game.GameResult.DEALER_WINS, game.getResult());
        assertEquals(Game.GameState.GAME_OVER, game.getState());


    }

    @Test
    @DisplayName("Should throw exception for 'double' when is not the player's turn")
    void playerDouble_ShouldThrowException_WhenNotPlayerTurn() {

        game.setStateForTesting(Game.GameState.DEALER_TURN);


        assertThrows(InvalidGameActionException.class, () -> {
            game.playerDouble();
        });


        verifyNoInteractions(mockDeck);
        verifyNoInteractions(mockPlayer);
    }

    @Test
    @DisplayName("Should throw exception for 'double' when player hand size is not 2")
    void playerDouble_ShouldThrowException_WhenHandSizeNot2() {

        //Arrange
        game.setStateForTesting(Game.GameState.PLAYER_TURN);
        List<Card> mockHand = new ArrayList<>();
        mockHand.add(new Card(Card.Suit.HEARTS, Card.Rank.ACE));
        when(mockPlayer.getHand()).thenReturn(mockHand);

        assertThrows(InvalidGameActionException.class, () -> {
            game.playerDouble();
        });

        verifyNoInteractions(mockDeck);

        verify(mockPlayer).getHand();
        verifyNoMoreInteractions(mockPlayer);


    }


}