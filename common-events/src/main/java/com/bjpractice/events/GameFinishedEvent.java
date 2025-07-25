package com.bjpractice.events;

import java.util.UUID;

public record GameFinishedEvent(
        UUID gameId,
        UUID betId,
        Long userId,
        String result,
        boolean playerHasBlackjack
) {}
