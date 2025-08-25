package com.bjpractice.game_core.kafka.producer;

import com.bjpractice.events.GameFinishedEvent;
import com.bjpractice.game_core.config.properties.KafkaTopics;
import com.bjpractice.events.PlayerDoubleEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameEventProducer {


    @Autowired
    private KafkaTopics kafkaTopics;

    private final KafkaTemplate <String, Object> kafkaTemplate;

    public void sendGameFinishedEvent(GameFinishedEvent event){

        log.info("Sending GameFinishedEvent for game id: {}", event.gameId());
        try {
            kafkaTemplate.send(kafkaTopics.games(), event);
        } catch (Exception e){

            log.error("Error al enviar GameFinishedEvent para gameId: {}", event.gameId(), e);
            // Aquí se podría implementar una lógica de reintento o de persistencia
            // del evento para no perderlo en caso de que Kafka esté caído.

            // La implementación de desearlo sería un Transactional Outbox pattern (Deuda técnica tbh)

        }
    }

    public void sendPlayerDoubledEvent(PlayerDoubleEvent event) {
        log.info("Enviando PlayerDoubledDownEvent para gameId: {}", event.gameId());
        try {
            kafkaTemplate.send(kafkaTopics.games(), event);
        } catch (Exception e) {
            log.error("Error al enviar PlayerDoubledDownEvent para gameId: {}", event.gameId(), e);
        }
    }


}
