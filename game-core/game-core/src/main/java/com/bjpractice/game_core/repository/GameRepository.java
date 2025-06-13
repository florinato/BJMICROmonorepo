package com.bjpractice.game_core.repository;

import com.bjpractice.game_core.model.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GameRepository extends JpaRepository<GameEntity, UUID> {


    Optional<GameEntity> findByBetId(UUID betId);
}
