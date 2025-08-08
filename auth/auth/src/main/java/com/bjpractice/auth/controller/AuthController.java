package com.bjpractice.auth.controller;

import com.bjpractice.auth.dto.JwtResponse;
import com.bjpractice.auth.dto.LoginRequest;
// Crearemos este DTO ahora
import com.bjpractice.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody @Valid LoginRequest request) {
        // 1. Llama al servicio para que haga todo el trabajo
        String token = authenticationService.login(request);

        // 2. Envuelve el token en un DTO de respuesta y lo devuelve
        return ResponseEntity.ok(new JwtResponse(token));
    }
}