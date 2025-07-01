package com.bjpractice.bets.kafka.event;

import java.util.UUID;

public record GameFinishedEvent(

        UUID gameId,
        UUID betId,
        Long userId,
        String result,
        boolean playerHasBlackJack
) {


}
