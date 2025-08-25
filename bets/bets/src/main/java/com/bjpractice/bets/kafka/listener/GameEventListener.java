package com.bjpractice.bets.kafka.listener;


import com.bjpractice.bets.bet.service.BetService;
import com.bjpractice.events.GameFinishedEvent;
import com.bjpractice.events.PlayerDoubleEvent;
import lombok.extern.slf4j.Slf4j;
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
            id = "gameFinishedListener",  // Definimos el id del listener, por control y mantenibilidad
            topics = "${app.kafka.topics.games}",
            groupId = "bets-service-group",
            containerFactory = "gameFinishedEventContainerFactory"
    )
    public void handleGameFinishedEvent(GameFinishedEvent event){

        log.info("SUCCESS: Received GameFinishedEvent for betId:  {}", event.betId());
        betService.processGameResult(event);

    }

    @KafkaListener(
            id = "playerDoubleListener",
            topics = "${app.kafka.topics.games}",
            groupId = "bets-service-group",
            containerFactory = "playerDoubleEventContainerFactory"
    )
    public void handlePlayerDouble(PlayerDoubleEvent event) {
        log.info("SUCCESS: Received PlayerDoubleEvent for betId: {}", event.betId());
        betService.processPlayerDouble(event);
    }




}
