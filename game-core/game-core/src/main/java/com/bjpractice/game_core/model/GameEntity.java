package com.bjpractice.game_core.model;


import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "games")
public class GameEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private Long userId;  // Mirar una vez se cree user si Long es el tipo adecuado

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Type(JsonType.class)
    @Column(columnDefinition = "json", nullable = false)
    private Game gameLogic;

    @Column(nullable = false, unique = true)
    private UUID betId;


    // Service stuff (preliminar)

    public GameEntity(UUID id, Long userId, UUID betId) {
        this.id = id;
        this.userId = userId;
        this.betId  = betId;
        this.gameLogic = new Game();
    }


}
