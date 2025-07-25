package com.bjpractice.bets.config;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {


    @Value("${kafka.topic.games}")
    private String gamesTopic;

    @Value("${app.kafka.topics.bet-settled}")
    private String betSettledTopic;

    @Bean
    public NewTopic gamesTopicBean() {
        return TopicBuilder.name(gamesTopic)
                .partitions(1) // Para tests, 1 partición es suficiente y más rápido
                .replicas(1)
                .build();
    }


    @Bean
    public NewTopic betSettledTopicBean() {
        return TopicBuilder.name(betSettledTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }



}
