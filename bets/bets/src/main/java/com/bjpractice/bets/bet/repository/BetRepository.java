package com.bjpractice.bets.bet.repository;

import com.bjpractice.bets.bet.model.Bet;
import com.bjpractice.bets.bet.model.BetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BetRepository extends JpaRepository<BetEntity, UUID> {

    Optional<Bet> findByGameId(UUID gameId);

}
