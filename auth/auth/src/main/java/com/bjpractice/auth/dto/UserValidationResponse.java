package com.bjpractice.auth.dto;

import com.bjpractice.auth.model.Role;

public record UserValidationResponse(
    Long id,
    String username,
    String passwordHash,
    Role role)
{}
