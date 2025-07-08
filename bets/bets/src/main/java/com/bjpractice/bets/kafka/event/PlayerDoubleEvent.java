package com.bjpractice.bets.kafka.event;

import java.util.UUID;

public record PlayerDoubleEvent(
        UUID gameId,
        UUID betId,
        Long userId
) {

}
