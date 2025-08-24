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
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {


    @Container
    static final GenericContainer<?> wiremockServer = new GenericContainer<>(DockerImageName.parse("wiremock/wiremock:3.5.4-alpine"))
            .withExposedPorts(8080);


    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {

        String host = wiremockServer.getHost();
        Integer port = wiremockServer.getMappedPort(8080);


        String wiremockUrl = String.format("http://%s:%d", host, port);
        registry.add("application.config.user-service-url", () -> wiremockUrl);
    }
}