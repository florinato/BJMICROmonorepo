package com.bjpractice.bets.bet.model;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;





@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "bets")
public class BetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    @PrePersist  // est emetodo se ejecutará antes de que la entidad se guarda en la db
    protected void onCreate(){

        if(createdAt == null){
            createdAt = LocalDateTime.now();
        }
        if (status == null){
            status = BetStatus.PENDING_GAME;
        }
    }

//    public BetEntity(UUID id, Long userId, UUID gameId, BigDecimal amount){
//
//        this.id = id;
//        this.userId = userId;
//        this.gameId = gameId;
//        this.amount = amount;
//        this.status = BetStatus.PENDING;
//    }
//
//    public BetEntity(Long userId, BigDecimal amount){
//
//        this.id = UUID.randomUUID();
//        this.userId = userId;
//        this.amount = amount;
//        this.status = BetStatus.PENDING;
//        this.createdAt = LocalDateTime.now();
//        this.gameId = null;
//
//    }
}
