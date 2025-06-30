package com.bjpractice.game_core.mapper;

import com.bjpractice.game_core.dto.CardDTO;
import com.bjpractice.game_core.dto.GameDTO;
import com.bjpractice.game_core.model.Game;
import com.bjpractice.game_core.model.GameEntity;
import com.bjpractice.game_core.model.Card;

import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.List;


/**
 * Componente responsable de la transformación entre objetos del dominio y DTOs.
 * Actúa como una capa de adaptación que:
 * - Convierte tipos complejos (Enums) a simples (Strings) para JSON.
 * - Oculta datos sensibles (como el mazo) del cliente.
 * - Aplica lógica de presentación (como ocultar la carta del dealer).
 * Esto permite que la lógica interna y la API pública evolucionen de forma independiente.
 */

@Component
public class GameMapper {

    public GameDTO toDTO(GameEntity gameEntity){

        if (gameEntity == null || gameEntity.getGameLogic() == null){
            return null;
        }

        Game game = gameEntity.getGameLogic();
        GameDTO dto = new GameDTO();

        dto.setGameId(gameEntity.getId());
        dto.setUserId(gameEntity.getUserId());
        dto.setGameState(game.getState());
        dto.setGameResult(game.getResult());

        dto.setPlayerHand(toCardDTOList(game.getPlayer().getHand()));
        dto.setPlayerScore(game.getPlayer().calculateHandValue());


        if (game.isGameOver()) {
            dto.setDealerHand(toCardDTOList(game.getDealer().getHand()));
            dto.setDealerScore(game.getDealer().calculateHandValue());

            // Si el juego está en curso, solo mostramos la segunda carta del dealer
        } else {

            List<CardDTO> visibleDealerHand = new ArrayList<>();

            if (game.getDealer().getHand().size() > 1) {
                visibleDealerHand.add(toCardDTO(game.getDealer().getHand().get(1)));
            }
            dto.setDealerHand(visibleDealerHand);

            // No mostramos la puntuación real del dealer hasta el final
            dto.setDealerScore(game.getDealer().getHand().get(1).getValue());
        }

        return dto;
    }



    public List<CardDTO> toCardDTOList(List<Card> cards) {

        return cards.stream().map(this::toCardDTO).toList();
    }

    public CardDTO toCardDTO(Card card) {

        return new CardDTO(card.getRank().name(), card.getSuit().name());

    }



    }

