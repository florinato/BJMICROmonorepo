package com.bjpractice.user.kafka.listener;


import com.bjpractice.user.kafka.event.BetSettledEvent;
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

        try {
            userService.updateBalance(event.userId(), event.amount());
            log.info("Saldo actualizado correctamente para userId: {}", event.userId());
        } catch (Exception e) {
            log.error("Error al procesar BetSettledEvent para userId: {}. Error: {}", event.userId(), e.getMessage());
            // Aquí se podría implementar una estrategia de reintentos o enviar a un Dead Letter Topic.
        }
    }



}
