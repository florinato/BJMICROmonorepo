package com.bjpractice.user.kafka.event;

import java.math.BigDecimal;

public record BetSettledEvent(
        Long userId,
        BigDecimal amount
) {}
