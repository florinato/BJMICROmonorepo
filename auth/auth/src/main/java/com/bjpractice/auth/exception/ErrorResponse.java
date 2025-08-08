package com.bjpractice.auth.exception;

import java.time.Instant;

public record ErrorResponse(
        int status,
        String error,
        String message,
        long timestamp
)
{
    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, Instant.now().toEpochMilli());
    }

}