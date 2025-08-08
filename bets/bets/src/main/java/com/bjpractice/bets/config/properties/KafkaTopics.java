package com.bjpractice.bets.config.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.topics") // This is the trick for a cohesive yaml properties file
public record KafkaTopics(String games, String betSettled)
{}
