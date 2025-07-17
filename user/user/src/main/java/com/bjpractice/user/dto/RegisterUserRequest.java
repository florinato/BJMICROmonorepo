package com.bjpractice.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank(message = "El nombre de usuario no puede estar vacío")
        String username,

        @NotBlank
        @Email(message = "El formato del email no es válido")
        String email,

        @NotBlank
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password
) {}
