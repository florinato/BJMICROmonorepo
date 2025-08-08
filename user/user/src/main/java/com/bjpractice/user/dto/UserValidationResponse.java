package com.bjpractice.user.dto;

import com.bjpractice.user.entity.User;

public record UserValidationResponse(
        Long id,
        String username,
        String passwordHash,
        User.Role role
) {}