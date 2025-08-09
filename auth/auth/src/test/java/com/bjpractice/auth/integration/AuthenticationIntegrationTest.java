package com.bjpractice.auth.integration;

import com.bjpractice.auth.dto.LoginRequest;
import com.bjpractice.dtos.UserValidationResponse;
import com.bjpractice.dtos.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthenticationIntegrationTest extends AbstractIntegrationTest{

    @Autowired
    private MockMvc mockMvc; // 2. Para simular peticiones HTTP

    @Autowired
    private ObjectMapper objectMapper; // 3. Para convertir objetos a JSON

    @Autowired
    private PasswordEncoder passwordEncoder; // 4. Para hashear la contraseña de prueba

    // --- NUEVO MÉTODO DE CONFIGURACIÓN ---
    // Se ejecuta una vez antes de todos los tests de esta clase.
    @BeforeAll
    static void setupWireMock() {
        // Le decimos al cliente estático de WireMock a qué URL debe apuntar.
        WireMock.configureFor(wiremockServer.getHost(), wiremockServer.getMappedPort(8080));
    }


    @Test
    void whenLoginWithValidCredentials_thenReturns200AndJwt() throws Exception {
        // --- ARRANGE (Preparación) ---
        String username = "testuser";
        String plainPassword = "password123";
        String hashedPassword = passwordEncoder.encode(plainPassword);

        UserValidationResponse userResponse = new UserValidationResponse(
                1L,
                username,
                hashedPassword,
                Role.USER
        );

        // --- CAMBIO AQUÍ ---
        // Ahora usamos el método estático directamente, sin "wiremockServer."
        stubFor(get(urlEqualTo("/api/v1/users/internal/validate/" + username))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(userResponse))
                ));

        LoginRequest loginRequest = new LoginRequest(username, plainPassword);


        // --- ACT & ASSERT (Sin cambios aquí) ---
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is(notNullValue())));
    }


}
