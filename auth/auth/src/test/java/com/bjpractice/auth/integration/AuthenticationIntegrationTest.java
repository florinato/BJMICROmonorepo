package com.bjpractice.auth.integration;


import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;

import org.testcontainers.utility.DockerImageName;


import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class AuthenticationIntegrationTest extends AbstractIntegrationTest{

    @Autowired
    private TestRestTemplate restTemplate;

    @Container
    static GenericContainer<?> authServiceContainer = new GenericContainer<>(
            // Le decimos a Testcontainers que construya la imagen desde un Dockerfile
            new ImageFromDockerfile()
                    .withDockerfile(Paths.get("../auth/auth/Dockerfile"))
    )
            .withExposedPorts(8084)
            .waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200));



    @BeforeAll
    static void setupWiremockStubs() {
        // --- NUEVO: Configuramos el cliente estático de WireMock ---
        // Le decimos que apunte a la IP y puerto dinámicos de nuestro contenedor.
        WireMock.configureFor(wiremockServer.getHost(), wiremockServer.getPort());

        // Preparamos el hash de una contraseña conocida ("password")
        String passwordHash = new BCryptPasswordEncoder().encode("password");

        // --- AHORA LAS LLAMADAS FUNCIONAN ---
        // Ya no llamamos a wiremockServer.stubFor(), sino directamente a stubFor()
        // porque es un método estático del cliente que acabamos de configurar.
        stubFor(
                get(urlEqualTo("/api/v1/users/internal/validate/testuser"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(String.format("""
                                    {
                                      "id": 1,
                                      "username": "testuser",
                                      "passwordHash": "%s",
                                      "role": "USER"
                                    }
                                    """, passwordHash))
                        )
        );

        stubFor(
                get(urlEqualTo("/api/v1/users/internal/validate/nonexistent"))
                        .willReturn(aResponse().withStatus(404))
        );
    }

    @Test
    @DisplayName("Integration Test: should return JWT when credentials are valid")
    void shouldReturnJwt_whenCredentialsAreValid() {
        // --- ARRANGE ---
        // Construimos la URL dinámica a nuestro auth-service en ejecución
        String url = "http://localhost:" + authServiceContainer.getMappedPort(8084) + "/api/v1/auth/login";

        // Creamos el cuerpo de la petición
        String requestBody = "{\"username\": \"testuser\", \"password\": \"password\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        // --- ACT ---
        // Hacemos la llamada POST real al contenedor
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        // --- ASSERT ---
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().startsWith("ey"), "The response body should be a JWT token");
    }

    @Test
    @DisplayName("Integration Test: should return 401 when user is not found")
    void shouldReturn401_whenUserIsNotFound() {
        // --- ARRANGE ---
        String url = "http://localhost:" + authServiceContainer.getMappedPort(8084) + "/api/v1/auth/login";
        String requestBody = "{\"username\": \"nonexistent\", \"password\": \"password\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        // --- ACT ---
        // TestRestTemplate no lanza excepción con errores 4xx/5xx, lo que nos permite inspeccionar la respuesta
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        // --- ASSERT ---
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), "Should return 401 due to our GlobalExceptionHandler");
        assertTrue(response.getBody().contains("Authentication Failed"), "The error message should be present");
    }

}
