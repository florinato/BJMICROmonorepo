package com.bjpractice.game_core.integration;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.MySQLContainer;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Testcontainers
public abstract class AbstractIntegrationTest {


    @Container
    @ServiceConnection
    protected static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0");


    @Container
    protected static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.0.1")).withKraft();

    @DynamicPropertySource
    static void setKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
    }


    /**
     * Método de ayuda para consumir un número esperado de eventos de un topic.
     * @param topic El topic del que consumir.
     * @param expectedRecordCount El número de registros que se espera recibir.
     * @return Los registros consumidos.
     */
    protected ConsumerRecords<String, Object> consumeEvents(String topic, int expectedRecordCount) {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "tc-test-group-" + UUID.randomUUID());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(Collections.singletonList(topic));

            // ✅ CORRECTION: Bucle de reintento para dar tiempo a que lleguen los eventos
            final int maxRetries = 10;
            int retries = 0;
            ConsumerRecords<String, Object> records = ConsumerRecords.empty();

            while (records.count() < expectedRecordCount && retries < maxRetries) {
                records = consumer.poll(Duration.ofSeconds(1)); // Hacemos polls más cortos y repetidos
                retries++;
            }

            assertEquals(expectedRecordCount, records.count(), "Se esperaba recibir " + expectedRecordCount + " evento(s)");
            return records;
        }
    }


}
