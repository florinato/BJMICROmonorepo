package com.bjpractice.bets.integration;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;



@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public abstract class AbstractBetIntegrationTest {


    // Think about refacotring to withKraft()

    private static final Network network = Network.newNetwork();

//    @Container
//    @ServiceConnection
//    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.0.1"))
//            .withKraft()
//            .withNetwork(network)
//            .withNetworkAliases("kafka")
//            .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://kafka:9092");

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.0.1"))
            // Eliminamos .withKraft() y todas las variables de entorno complejas
            .withNetwork(network)
            .withNetworkAliases("kafka");

    @Container
    @ServiceConnection
    static MySQLContainer<?> betsDb = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("bjmicros_bets_db")
            .withNetwork(network);

    @Container
    static MySQLContainer<?> userDb = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("bjmicros_users_db")
            .withNetwork(network);

    @Container
    static GenericContainer<?> userServiceContainer = new GenericContainer<>("user:0.0.1-SNAPSHOT")
            .withExposedPorts(8083)
            .dependsOn(userDb, kafka)
            .withNetwork(network)
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:mysql://" + userDb.getNetworkAliases().get(0) + ":3306/" + userDb.getDatabaseName())
            .withEnv("SPRING_DATASOURCE_USERNAME", userDb.getUsername())
            .withEnv("SPRING_DATASOURCE_PASSWORD", userDb.getPassword())
            .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")
            .withEnv("management.health.kafka.enabled", "false")
            .withEnv("APP_KAFKA_TOPICS_BET-SETTLED", "bets.bet-settled-test")
            .waitingFor(Wait.forHealthcheck())
            .withLogConsumer(new Slf4jLogConsumer(log));


}




