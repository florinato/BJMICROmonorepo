package com.bjpractice.events;

import java.math.BigDecimal;


public record BetSettledEvent (
        Long userId,
        BigDecimal amount
) {}
