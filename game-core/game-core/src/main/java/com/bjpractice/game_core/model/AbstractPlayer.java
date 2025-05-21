package com.bjpractice.game_core.model;


import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPlayer {

    protected List<Card> hand = new ArrayList<>();

    public void receiveCard(Card card) {
        hand.add(card);
    }

    public void clearHand() {
        hand.clear();
    }


    public List<Card> getHand() {
        return hand;
    }

    public int calculateHandValue() {
        int value = 0;
        int aceCount = 0;

        for (Card card : hand) {
            value += card.getValue();
            if (card.isAce()) {
                aceCount++;
            }
        }

        while (value > 21 && aceCount > 0) {
            value -= 10;
            aceCount--;
        }

        return value;
    }


    public boolean isBust() {
        return calculateHandValue() > 21;
    }


    public boolean hasBlackjack() {
        return hand.size() == 2 && calculateHandValue() == 21;
    }


}