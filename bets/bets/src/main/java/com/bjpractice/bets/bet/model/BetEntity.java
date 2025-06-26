package com.bjpractice.bets.bet.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor


@Entity
@Table(name = "bets")
public class BetEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private Long userId;

    @Column (nullable = true) // El id se genera en game service
    private UUID gameId;

    @Column(nullable = false, precision = 19, scale = 4) // Buena práctica para dinero
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BetStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public BetEntity(UUID id, Long userId, UUID gameId, BigDecimal amount){

        this.id = id;
        this.userId = userId;
        this.gameId = gameId;
        this.amount = amount;
        this.status = BetStatus.PENDING;
    }

    public BetEntity(Long userId, BigDecimal amount){

        this.id = UUID.randomUUID();
        this.userId = userId;
        this.amount = amount;
        this.status = BetStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.gameId = null;

    }
}
