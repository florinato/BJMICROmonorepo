package com.bjpractice.auth.integration;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AbstractIntegrationTest {


    @Container
    static WireMockContainer wiremockServer = new WireMockContainer("wiremock/wiremock:3.5.2-1")
            .withExposedPorts(8080);


    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        String wiremockUrl = wiremockServer.getBaseUrl();
        registry.add("application.config.user-service-url", () -> wiremockUrl);
        // Si tuviéramos más servicios comunes (BD, Kafka), sus propiedades irían aquí también.
    }
}
