package com.bjpractice.bets.bet.controller;


// UNDER CONSTRUCTION


//@RestController
//@RequestMapping("/api/bets")
//public class BetsController {
//
//    private final BetService betService;
//    // ... constructor ...
//
//    @PostMapping
//    public ResponseEntity<BetDTO> placeBet(
//            @RequestHeader("X-User-ID") Long userId, // <-- APISIX nos lo da aquí
//            @RequestBody CreateBetRequest betRequest // <-- El cuerpo ya no necesita el userId
//    ) {
//        // Le pasamos al servicio el userId que vino de la cabecera
//        BetDTO newBet = betService.placeBet(userId, betRequest.getAmount());
//        return ResponseEntity.status(HttpStatus.CREATED).body(newBet);
//    }
//}