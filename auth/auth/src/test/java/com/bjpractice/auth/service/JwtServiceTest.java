package com.bjpractice.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.bjpractice.dtos.model.Role;

import java.security.Key;

import static org.junit.jupiter.api.Assertions.*;

// Solo queremos saber que el token se genera adecuadamente, pues gestionar los claims será trabajo del Gateway!

public class JwtServiceTest {

    private JwtService jwtService;

    private Key signingKey;

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

        this.signingKey = (Key) ReflectionTestUtils.getField(jwtService, "key");
    }

    @Test
    void shouldGenerateTokenWithCorrectClaims() {
        // --- Arrange ---
        Long expectedUserId = 123L;
        Role expectedRole = Role.USER;

        // --- Act  ---
        String token = jwtService.generateToken(expectedUserId, expectedRole);

        // --- Assert  ---
        assertNotNull(token);

        assertEquals(expectedUserId, extractUserIdFromToken(token));
        assertEquals(expectedRole.name(), extractRoleFromToken(token));
    }

    // ---- MÉTODOS DE AYUDA PRIVADOS DEL TEST ----

    private Claims extractAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(this.signingKey) // Usamos la clave que guardamos
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Long extractUserIdFromToken(String token) {
        return extractAllClaimsFromToken(token).get("userId", Long.class);
    }

    private String extractRoleFromToken(String token) {
        return extractAllClaimsFromToken(token).get("role", String.class);
    }


}
