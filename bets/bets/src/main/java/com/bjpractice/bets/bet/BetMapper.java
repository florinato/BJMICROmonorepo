package com.bjpractice.bets.bet;

import com.bjpractice.bets.bet.model.BetEntity;
import org.springframework.stereotype.Component;

/**
 * Componente responsable de la transformación entre la entidad BetEntity y el DTO BetDTO.
 * Desacopla el modelo de persistencia del contrato de la API.
 */
@Component // Lo marcamos como un componente de Spring para poder inyectarlo en el servicio
public class BetMapper {

    /**
     * Convierte una BetEntity a su representación en BetDTO.
     * @param entity La entidad de la apuesta obtenida de la base de datos.
     * @return El DTO correspondiente.
     */
    public BetDTO toDTO(BetEntity entity) {
        if (entity == null) {
            return null;
        }

        BetDTO dto = new BetDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setGameId(entity.getGameId());
        dto.setAmount(entity.getAmount());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());

        return dto;
    }
}