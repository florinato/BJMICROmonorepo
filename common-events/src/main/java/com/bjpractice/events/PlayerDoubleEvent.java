package com.bjpractice.events;



import java.util.UUID;


public record PlayerDoubleEvent (

    UUID gameId,
    UUID betId,
    Long userId)
{
}
