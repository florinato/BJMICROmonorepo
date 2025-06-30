package com.bjpractice.bets.bet.service;


import com.bjpractice.bets.bet.model.BetEntity;
import com.bjpractice.bets.bet.repository.BetRepository;
import com.bjpractice.bets.bet.BetDTO;
import com.bjpractice.bets.bet.BetMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


// UNDER CONSTRUCTION !!!!


@Service
public class BetService {

    private final BetRepository betRepository;
    private final BetMapper betMapper;

    public BetService(BetRepository betRepository, BetMapper betMapper){
        this.betRepository = betRepository;
        this.betMapper = betMapper;
    }


    /**
     * Crea una nueva apuesta para un usuario.
     * Este es el primer paso en el flujo del juego.
     *
     * @param userId El ID del usuario que realiza la apuesta.
     * @param amount El monto de la apuesta.
     * @return Un DTO con la información de la apuesta creada.
     */
    public BetDTO placeBet(Long userId, BigDecimal amount) {
        // --- 1. Validación (Preliminar) ---
        // Aquí podríamos añadir validaciones importantes en el futuro:
        // - ¿El 'amount' es positivo?
        // - ¿El 'userId' existe y tiene fondos suficientes? (requeriría comunicación con user-service)
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            // throw new InvalidBetAmountException("El monto de la apuesta debe ser positivo.");
        }

        // --- 2. Creación de la Entidad ---
        // Usamos el nuevo constructor que no requiere un gameId.
        BetEntity newBet = new BetEntity(userId, amount);

        // --- 3. Persistencia ---
        // Guardamos la nueva apuesta en la base de datos.
        BetEntity savedBet = betRepository.save(newBet);

        // --- 4. Devolver Respuesta ---
        // Mapeamos la entidad guardada a un DTO para devolverla al cliente.
        // El cliente obtendrá el 'betId' de este DTO para luego iniciar el juego.
        return betMapper.toDTO(savedBet);
    }




}
