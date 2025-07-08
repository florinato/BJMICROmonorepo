package com.bjpractice.bets.config;


import com.bjpractice.bets.client.UserServiceClient;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestUserClientConfiguration {

    @Bean
    @Primary
    public UserServiceClient userServiceClient(){

        return Mockito.mock(UserServiceClient.class);
    }
}
