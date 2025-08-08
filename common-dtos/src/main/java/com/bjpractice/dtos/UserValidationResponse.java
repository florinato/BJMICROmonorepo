package com.bjpractice.dtos;

import com.bjpractice.dtos.model.Role;


public record UserValidationResponse(
        Long id,
        String username,
        String passwordHash,
        Role role
) {}