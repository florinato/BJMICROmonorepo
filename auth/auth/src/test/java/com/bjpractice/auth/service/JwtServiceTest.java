package com.bjpractice.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    public void setUp(){

// Arrange

        jwtService = new JwtService();

// usar reflexión es malo. Pero usar ReflectionTestUtils en un test unitario
// para inyectar una dependencia que normalmente pone Spring es un "mal menor" aceptado y pragmático.
        // Esto evita tener que cargar  el contexto de Spring para una prueba unitaria.
        String secret = "c3VwZXItc2VjcmV0LWtleS1mb3ItZGV2ZWxvcG1lbnQtZW52aXJvbm1lbnQ=";
        long expiration = 86400000L;

        ReflectionTestUtils.setField(jwtService, "secretKey", secret);
        ReflectionTestUtils.setField(jwtService,"jwtExpiration", expiration);

        // @PostConstruct no se llama en los tests unitarios so lo hacemos manualmente
        jwtService.init();

    }

    @Test
    void shouldGenerateTokenAndExtractCorrectUserId() {
        // --- Arrange ---
        Long expectedUserId = 123L;
        Role userRole = Role.USER;

        // --- Act (Actuar) ---
        String token = jwtService.generateToken(expectedUserId, userRole);
        Long extractedUserId = jwtService.extractUserId(token);

        // --- Assert (Verificar) ---
        assertNotNull(token, "El token no debería ser nulo");
        assertFalse(token.isEmpty(), "El token no debería estar vacío");
        assertEquals(expectedUserId, extractedUserId, "El ID del usuario extraído del token no coincide con el original");
    }


}
