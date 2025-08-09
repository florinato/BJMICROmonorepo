package com.bjpractice.auth.integration;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test") // Asegura que se cargue application-test.yml si lo tuvieras
public abstract class AbstractIntegrationTest {

    // 1. Declaración del Contenedor de WireMock
    @Container
    static final GenericContainer<?> wiremockServer = new GenericContainer<>(DockerImageName.parse("wiremock/wiremock:3.5.4-alpine"))
            .withExposedPorts(8080); // Exponemos el puerto interno del contenedor

    // 2. Configuración Dinámica de Propiedades
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        // Obtenemos el host y el puerto mapeado aleatoriamente por Testcontainers
        String host = wiremockServer.getHost();
        Integer port = wiremockServer.getMappedPort(8080);

        // 3. Sobrescribimos la propiedad de la URL del user-service
        String wiremockUrl = String.format("http://%s:%d", host, port);
        registry.add("application.config.user-service-url", () -> wiremockUrl);
    }
}