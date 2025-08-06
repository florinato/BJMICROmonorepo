package com.bjpractice.bets.kafka.producer;


import com.bjpractice.bets.client.UserServiceClient;
import com.bjpractice.events.BetSettledEvent;
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


        BetSettledEvent event = new BetSettledEvent(userId, amount);

        try {
            // Hacemos la llamada síncrona: esperamos hasta 2 segundos por la confirmación.
            kafkaTemplate.send(betSettledTopic, event).get(2, java.util.concurrent.TimeUnit.SECONDS);
            log.info("CONFIRMADO: BetSettledEvent enviado para userId: {}", userId);
        } catch (Exception e) {
            log.error("FALLO al enviar BetSettledEvent de forma síncrona", e);
            // Relanzamos la excepción para que el test falle si el envío no funciona.
            throw new RuntimeException(e);
        }

    }

}
