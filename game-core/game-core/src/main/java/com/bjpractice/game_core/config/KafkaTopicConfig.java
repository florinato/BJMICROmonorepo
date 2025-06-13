package com.bjpractice.game_core.config;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.games}")
    private String gamesTopic;

    @Bean
    public NewTopic gamesTopic() {

        return TopicBuilder.name(gamesTopic)
                .partitions(6)
                .replicas(1)
                .build();
    }
}
