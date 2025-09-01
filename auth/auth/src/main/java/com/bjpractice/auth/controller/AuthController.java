package com.bjpractice.auth.controller;

import com.bjpractice.auth.dto.JwtResponse;
import com.bjpractice.auth.dto.LoginRequest;

import com.bjpractice.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Controlador REST que gestiona las peticiones de autenticación.
 * Expone los endpoints para el inicio de sesión y la validación de credenciales.
 */

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Autentica a un usuario a partir de sus credenciales.
     *
     * @param request El objeto de solicitud de login, que contiene el nombre de usuario y la contraseña.
     * @return Un ResponseEntity que contiene un JwtResponse con el token de acceso si la autenticación es exitosa.
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody @Valid LoginRequest request) {

        String token = authenticationService.login(request);


        return ResponseEntity.ok(new JwtResponse(token));
    }
}