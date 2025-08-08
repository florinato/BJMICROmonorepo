package com.bjpractice.auth.service;


import com.bjpractice.auth.dto.LoginRequest;
import com.bjpractice.auth.model.CustomUserDetails;
import com.bjpractice.dtos.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    // --- Dependencias a mockear ---
    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;


    @Mock
    private Authentication authentication;


    @InjectMocks
    private AuthenticationService authenticationService;


    private CustomUserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        // Creamos un usuario de prueba que simularemos nos devuelve el AuthenticationManager
        testUserDetails = new CustomUserDetails(
                1L,
                Role.USER,
                "password_hash",
                "testPassword"
        );
    }

    @Test
    @DisplayName("Debe devolver un token JWT cuando las credenciales son válidas")
    void shouldReturnJwtToken_WhenCredentialsAreValid() {
        // --- ARRANGE  ---
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        String expectedToken = "fake.jwt.token";

        // 1. Cuando el manager autentica, devuelve nuestro objeto 'authentication' mockeado.
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // 2. Cuando se pide el 'principal' de ese objeto, devuelve nuestros datos de usuario de prueba.
        when(authentication.getPrincipal()).thenReturn(testUserDetails);

        // 3. Cuando el servicio de JWT genera el token con los datos de nuestro usuario, devuelve el token esperado.
        when(jwtService.generateToken(testUserDetails.getId(), testUserDetails.getRole()))
                .thenReturn(expectedToken);


        // ACT


        String actualToken = authenticationService.login(loginRequest);

        // ASSERT
        assertNotNull(actualToken);
        assertEquals(expectedToken, actualToken, "El token devuelto no es el esperado.");


    }

    @Test
    @DisplayName("Debe lanzar BadCredentialsException cuando las credenciales son inválidas")
    void shouldThrowBadCredentials_WhenCredentialsAreInvalid() {
        // --- ARRANGE (Preparar) ---
        LoginRequest loginRequest = new LoginRequest("testuser", "wrong_password");

        // Configuramos el manager para que lance la excepción al intentar autenticar.
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        // --- ACT & ASSERT (Actuar y Verificar) ---
        assertThrows(BadCredentialsException.class, () -> {
            authenticationService.login(loginRequest);
        }, "Se esperaba que se lanzara una BadCredentialsException.");
    }


}
