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
class AuthenticationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @BeforeAll
    static void setupWireMock() {

        WireMock.configureFor(wiremockServer.getHost(), wiremockServer.getMappedPort(8080));
    }


    @Test
    void whenLoginWithValidCredentials_thenReturns200AndJwt() throws Exception {
        // --- ARRANGE  ---
        String username = "testuser";
        String plainPassword = "password123";
        String hashedPassword = passwordEncoder.encode(plainPassword);

        UserValidationResponse userResponse = new UserValidationResponse(
                1L,
                username,
                hashedPassword,
                Role.USER
        );


        stubFor(get(urlEqualTo("/api/v1/users/internal/validate/" + username))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(userResponse))
                ));

        LoginRequest loginRequest = new LoginRequest(username, plainPassword);


        // --- ACT & ASSERT
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is(notNullValue())));
    }


    @Test
    void whenLoginWithNonExistentUser_thenReturns401() throws Exception {
        // --- ARRANGE

        //El test no debe dar excesivos detalles
//        el AuthenticationManager de Spring Security la captura y,
//        para evitar ataques de "enumeración de usuarios", la enmascara deliberadamente como una BadCredentialsException.
//        De esta forma, un atacante no puede distinguir entre un usuario que no existe y una contraseña incorrecta,
//        haciendo más difícil adivinar nombres de usuario válidos.

        String nonExistentUsername = "noexisto";


        stubFor(get(urlEqualTo("/api/v1/users/internal/validate/" + nonExistentUsername))
                .willReturn(aResponse()
                        .withStatus(404)
                ));


        LoginRequest loginRequest = new LoginRequest(nonExistentUsername, "any-password");


        // --- ACT & ASSERT
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))

                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Credenciales Inválidas")))
                .andExpect(jsonPath("$.message", is("El nombre de usuario o la contraseña son incorrectos.")));
    }


    @Test
    void whenUserServiceFails_thenReturns500() throws Exception {
        // --- ARRANGE


        String username = "anyuser";


        stubFor(get(urlEqualTo("/api/v1/users/internal/validate/" + username))
                .willReturn(aResponse()
                        .withStatus(500) // <-- Simulamos un Internal Server Error
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"database is down\"}")
                ));


        LoginRequest loginRequest = new LoginRequest(username, "any-password");


        // --- ACT & ASSERT
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))

                .andExpect(status().isInternalServerError())

                .andExpect(jsonPath("$.error", is("Error Interno del Servidor")));
    }




}
