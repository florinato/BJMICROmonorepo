package com.bjpractice.user.config;


import com.bjpractice.user.config.properties.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    private final KafkaTopics kafkaTopics;

    public KafkaTopicConfig(KafkaTopics kafkaTopics) {
        this.kafkaTopics = kafkaTopics;
    }

    @Bean
    public NewTopic betSettledTopicBean() {
        return TopicBuilder.name(kafkaTopics.betSettled())
                .partitions(1)
                .replicas(1)
                .build();
    }


}
