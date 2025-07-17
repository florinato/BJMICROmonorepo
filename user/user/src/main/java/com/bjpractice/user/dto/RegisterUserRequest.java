package com.bjpractice.user.dto;

public record RegisterUserRequest(
        String username,
        String email,
        String password
) {}
