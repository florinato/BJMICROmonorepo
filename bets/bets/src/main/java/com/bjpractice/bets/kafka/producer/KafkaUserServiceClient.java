package com.bjpractice.bets.kafka.producer;


import com.bjpractice.bets.client.UserServiceClient;
import com.bjpractice.bets.kafka.event.BetSettledEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class KafkaUserServiceClient implements UserServiceClient {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private String betSettledTopic;

    public KafkaUserServiceClient(KafkaTemplate<String, Object> kafkaTemplate,
                                  @Value("${app.kafka.topics.bet-settled}") String betSettledTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.betSettledTopic = betSettledTopic;
    }

    @Override
    public void creditUser(Long userId, BigDecimal amount) {
        log.info("Enviando BetSettledEvent para userId: {} con monto: {}", userId, amount);
        try {
            BetSettledEvent event = new BetSettledEvent(userId, amount);
            kafkaTemplate.send(betSettledTopic, event);
        } catch (Exception e) {
            log.error("No se pudo enviar BetSettledEvent para userId: {}", userId, e);
        }
    }


}
