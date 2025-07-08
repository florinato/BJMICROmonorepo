package com.bjpractice.bets.client;

import java.math.BigDecimal;

public interface UserServiceClient {

    void creditUser(Long userId, BigDecimal amount);

}
