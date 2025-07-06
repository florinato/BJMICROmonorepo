package com.bjpractice.bets.bet.controller;

import com.bjpractice.bets.bet.BetDTO;
import com.bjpractice.bets.bet.CreateBetRequest;
import com.bjpractice.bets.bet.service.BetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/bets")
public class BetController {

    private final BetService betService;

    public BetController(BetService betService) {

        this.betService = betService;
    }

    @PostMapping
    public ResponseEntity<BetDTO> placeBet(
            @RequestHeader("X-User-ID") Long userId,
            @RequestBody CreateBetRequest request)

    {

        BetDTO newBet = betService.placeBet(userId, request.amount());
        return ResponseEntity.status(HttpStatus.CREATED).body(newBet);
    }


}