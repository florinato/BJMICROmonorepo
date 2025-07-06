package com.bjpractice.bets.config;


import org.apache.kafka.common.errors.SerializationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;


import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler errorHandler() {

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(new FixedBackOff(0L, 1L));

        errorHandler.addNotRetryableExceptions(SerializationException.class);

        return errorHandler;
    }
}
