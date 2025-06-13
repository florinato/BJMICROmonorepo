package com.bjpractice.game_core.kafka.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDoubleEvent {

    private UUID gameId;

    private Long userId;


}
