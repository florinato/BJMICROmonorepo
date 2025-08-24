package com.bjpractice.game_core.model;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



public class GameEntityTestBuilder {

    // GAME STATES

    public static GameEntity createGameInPlayerTurn(Long userId, UUID betId) {

        GameEntity entity = new GameEntity(UUID.randomUUID(), userId, betId);
        Game game = entity.getGameLogic();

        game.startGame();

        return entity;
    }

    public static GameEntity createFinishedGame(Long userId, UUID betId) {

        GameEntity entity = createGameInPlayerTurn(userId, betId);
        Game game = entity.getGameLogic();

        game.setStateForTesting(Game.GameState.GAME_OVER);
        game.setResultForTesting(game.determineWinner());

        return entity;
    }


    // CARD BUILDER

    public static GameEntity createGameInProgressWithSpecificCards(
            Long userId,
            UUID betId,
            List<Card> playerHand,
            List<Card> dealerHand
    ) {



        GameEntity entity = new GameEntity(UUID.randomUUID(), userId, betId);
        Game game = entity.getGameLogic();

        game.getDeck().initializeDeck();
        game.getDeck().shuffle();

        Player player = new Player();
        playerHand.forEach(player::receiveCard);

        Dealer dealer = new Dealer();
        dealerHand.forEach(dealer::receiveCard);

        game.setPlayerForTesting(player);
        game.setDealerForTesting(dealer);
        game.setStateForTesting(Game.GameState.PLAYER_TURN);

        return entity;
    }

    public static GameEntity createFinishedGameWithSpecificCards(
            Long userId,
            UUID betId,
            List<Card> playerCards,
            List<Card> dealerCards) {

        GameEntity entity = createGameInProgressWithSpecificCards(userId, betId, playerCards, dealerCards);


        Game game = entity.getGameLogic();
        game.setStateForTesting(Game.GameState.GAME_OVER);


        Game.GameResult result = game.determineWinner();

        game.setResultForTesting(result);

        return entity;
    }

    // BUSTED DECK

    public static GameEntity createGameWithPredefinedDeck(Long userId, UUID betId, List<Card> playerCards, List<Card> dealerCards, List<Card> remainingDeck) {
        GameEntity entity = new GameEntity(UUID.randomUUID(), userId, betId);
        Game game = entity.getGameLogic();

        Player player = new Player();
        playerCards.forEach(player::receiveCard);
        Dealer dealer = new Dealer();
        dealerCards.forEach(dealer::receiveCard);


        Deck specificDeck = new Deck();
        specificDeck.setDeckList(new ArrayList<>(remainingDeck));

        game.setPlayerForTesting(player);
        game.setDealerForTesting(dealer);
        game.setDeckForTesting(specificDeck);
        game.setStateForTesting(Game.GameState.PLAYER_TURN);

        return entity;
    }
}





