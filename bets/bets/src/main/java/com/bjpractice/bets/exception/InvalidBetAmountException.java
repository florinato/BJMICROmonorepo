package com.bjpractice.bets.exception;

public class InvalidBetAmountException extends RuntimeException {
    public InvalidBetAmountException(String message) {
        super(message);
    }
}
