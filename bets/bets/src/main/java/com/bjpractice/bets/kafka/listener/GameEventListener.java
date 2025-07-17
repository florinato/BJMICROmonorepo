package com.bjpractice.bets.kafka.listener;


import com.bjpractice.bets.bet.service.BetService;
import com.bjpractice.bets.kafka.event.GameFinishedEvent;
import com.bjpractice.bets.kafka.event.PlayerDoubleEvent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GameEventListener {


    private final BetService betService;

    public GameEventListener(BetService betService) {
        this.betService = betService;
    }

    @KafkaListener(
            topics = "${kafka.topic.games}",
            groupId = "bets-service-group"
    )
    public void handleGameFinishedEvent(GameFinishedEvent event){

        log.info("SUCCESS: Received GameFinishedEvent for betId:  {}", event.betId());
        betService.processGameResult(event);
        // PLACEHOLDER PAL FUTURO
        // Aquí es donde, en el futuro, buscaríamos la apuesta por su betId
        // y la actualizaríamos con el resultado del juego.
        // Por ahora, solo registrar el evento es suficiente.
        // Ejemplo: betService.processGameResult(event.betId(), event.result());
    }

    @KafkaListener(
            topics = "${kafka.topic.games}",
            groupId = "bets-service-group"
    )
    public void handlePlayerDouble(PlayerDoubleEvent event) {
        log.info("SUCCESS: Received PlayerDoubleEvent for betId: {}", event.betId());
        // Llamamos al nuevo método que creamos en BetService
        betService.processPlayerDouble(event);
    }




}
