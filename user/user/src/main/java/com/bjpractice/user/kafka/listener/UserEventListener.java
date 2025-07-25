package com.bjpractice.user.kafka.listener;


import com.bjpractice.events.BetSettledEvent;
import com.bjpractice.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class UserEventListener {

    private final UserService userService;

    public UserEventListener(UserService userService) {
        this.userService = userService;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.bet-settled}",
            groupId = "user-service"
    )
    public void handleBetSettledEvent(BetSettledEvent event) {
        log.info("Evento BetSettledEvent recibido para userId: {}", event.userId());


        // Problem?
        userService.updateBalance(event.userId(), event.amount());
        log.info("Saldo actualizado correctamente para userId: {}", event.userId());

    }



}
