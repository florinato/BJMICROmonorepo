package com.bjpractice.bets.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer; // <--- Estudiar esto más


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
//    @ServiceConnection
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withKraft();

    @Container
//    @ServiceConnection
    static final MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("bets_db")
            .withUsername("test")
            .withPassword("test");


    @Container
    static final WireMockContainer wiremockServer = new WireMockContainer("wiremock/wiremock:3.5.4-alpine");




//    @DynamicPropertySource
//    static void setProperties(DynamicPropertyRegistry registry) {
//        // La configuración de Kafka y MySQL ahora es automática gracias a @ServiceConnection.
//        // Solo necesitamos registrar la propiedad para nuestro mock de WireMock.
//        registry.add("game-core.api.url", wiremockServer::getBaseUrl);
//    }

    //


    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        // Configuración de Kafka
//        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);


        registry.add("spring.kafka.consumer.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.admin.properties.bootstrap.servers", kafka::getBootstrapServers);

        // Configuración de la Base de Datos
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

        // Configuración del Mock Server
        // La propiedad 'game-core.api.url' debe existir en tu application.properties
        // para que tu UserServiceClient la use.
        registry.add("game-core.api.url", wiremockServer::getBaseUrl);
    }
}

