package com.bjpractice.game_core.model;

import com.bjpractice.game_core.exception.InvalidGameActionException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

public class Game {

    public enum GameState {
        WAITING_TO_START,
        PLAYER_TURN,
        DEALER_TURN,
        GAME_OVER,

    }

    public enum GameResult {
        PLAYER_WINS,
        DEALER_WINS,
        PUSH
    }

    @Getter
    private Player player;
    @Getter
    private Dealer dealer;
    @Getter
    @Setter
    private Deck deck;
    @Getter
    private GameState state;
    @Getter
    private GameResult result;


    public Game() {

        this.player = new Player();
        this.dealer = new Dealer();
        this.deck = new Deck();
        this.state = GameState.WAITING_TO_START;
        this.result = null;

    }


    // LOGIC

    public boolean startGame() {
        if (state != GameState.WAITING_TO_START) {
            throw new InvalidGameActionException("Action 'start' is not allowed when game state is " + state);
        }

        deck.initializeDeck();
        deck.shuffle();

        deck.dealCards(player, 2);
        deck.dealCards(dealer, 2);

        state = GameState.PLAYER_TURN;

        if (player.hasBlackjack() || dealer.hasBlackjack()) {
            state = GameState.GAME_OVER;
            result = determineWinner();
            return true; // <-- Informamos que el juego ha terminado.
        } else {
            state = GameState.PLAYER_TURN;
            return false; // <-- Informamos que el juego continúa.
        }

    }


    public void playDealerHand() {
        while (dealer.calculateHandValue() < 17) {
            dealer.receiveCard(deck.dealCard());
        }
    }

    public GameResult determineWinner() {
        if (player.isBust()) {
            return GameResult.DEALER_WINS;
        } else if (dealer.isBust()) {
            return GameResult.PLAYER_WINS;
        } else if (player.calculateHandValue() > dealer.calculateHandValue()) {
            return GameResult.PLAYER_WINS;
        } else if (player.calculateHandValue() < dealer.calculateHandValue()) {
            return GameResult.DEALER_WINS;
        } else {
            return GameResult.PUSH;
        }
    }

    @JsonIgnore
    public boolean isGameOver() {
        return state == GameState.GAME_OVER;
    }


    // ACTIONS

    public void playerHit() {
        if (state != GameState.PLAYER_TURN) {
            throw new InvalidGameActionException("Action 'hit' not allowed when game state is" + state);
        }
        player.receiveCard(deck.dealCard());
        if (player.isBust()) {
            result = GameResult.DEALER_WINS;
            state = GameState.GAME_OVER;
        }
    }


    public void playerStand() {
        if (state != GameState.PLAYER_TURN) {
            throw new InvalidGameActionException("Action stand not allowed when game stateis" + state);
        }
        state = GameState.DEALER_TURN;
        playDealerHand();
        result = determineWinner();
        state = GameState.GAME_OVER;
    }


    public void playerDouble() {

        if (state != GameState.PLAYER_TURN) {
            throw new InvalidGameActionException("Action 'double' is not allowed when game state is " + state);
        }
        int handSize = player.getHand().size();
        if (handSize != 2) {
            throw new InvalidGameActionException("Action 'double' is only allowed with the initial two cards, but player has " + handSize +  " cards.");
        }


        player.receiveCard(deck.dealCard());
        if (player.isBust()) {
            result = GameResult.DEALER_WINS;
        } else {
            state = GameState.DEALER_TURN;
            playDealerHand();
            result = determineWinner();
        }
        state = GameState.GAME_OVER;
    }

    // TESTING STUFF (Private package)

    void setStateForTesting(GameState state) {
        this.state = state;
    }

    void setDeckForTesting(Deck deck) {
        this.deck = deck;
    }

    void setPlayerForTesting(Player player) {
        this.player = player;
    }

    void setDealerForTesting(Dealer dealer) {
        this.dealer = dealer;
    }

    void setResultForTesting(Game.GameResult result) {
        this.result = result;
    }
}
