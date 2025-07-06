package com.bjpractice.game_core.config;


import com.bjpractice.game_core.kafka.producer.GameEventProducer;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestKafkaConfiguration {

    // @MockBean está deprecated por su seguirdad, en su lugar haremos:
    @Bean
    @Primary
    public GameEventProducer gameEventProducer(){

        return Mockito.mock(GameEventProducer.class);

    }

}
