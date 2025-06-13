package com.bjpractice.game_core.kafka.producer;

import com.bjpractice.game_core.kafka.event.PlayerDoubleEvent;
import com.bjpractice.game_core.kafka.event.GameFinishedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameEventProducer {

    private static final String TOPIC_GAMES = "games";

    private final KafkaTemplate <String, Object> kafkaTemplate;

    public void sendGameFinishedEvent(GameFinishedEvent event){

        log.info("Sending GameFinishedEvent for game id: {}", event.getGameId());
        try {
            kafkaTemplate.send(TOPIC_GAMES, event);
        } catch (Exception e){

            log.error("Error al enviar GameFinishedEvent para gameId: {}", event.getGameId(), e);
            // Aquí se podría implementar una lógica de reintento o de persistencia
            // del evento para no perderlo en caso de que Kafka esté caído.

        }
    }

    public void sendPlayerDoubledEvent(PlayerDoubleEvent event) {
        log.info("Enviando PlayerDoubledDownEvent para gameId: {}", event.getGameId());
        try {
            kafkaTemplate.send(TOPIC_GAMES, event);
        } catch (Exception e) {
            log.error("Error al enviar PlayerDoubledDownEvent para gameId: {}", event.getGameId(), e);
        }
    }


}
