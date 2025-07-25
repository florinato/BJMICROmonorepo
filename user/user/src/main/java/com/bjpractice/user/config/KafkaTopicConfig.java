package com.bjpractice.user.config;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topics.bet-settled}")
    private String betSettledTopic;

    @Bean
    public NewTopic betSettledTopicBean() {
        return TopicBuilder.name(betSettledTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }


}
