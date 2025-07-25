package com.bjpractice.game_core.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO {

    private String rank;
    private String suit;

}

//public record CardDTO(
//        String rank,
//        String suit
//){}
