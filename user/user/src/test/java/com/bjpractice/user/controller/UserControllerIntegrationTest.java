package com.bjpractice.user.controller;

import com.bjpractice.user.dto.RegisterUserRequest;
import com.bjpractice.user.entity.User;
import com.bjpractice.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    // Inicia un contenedor MySQL para los tests
    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.0.1")).withKraft();


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        userRepository.deleteAll();
    }

    @Test
    void registerUser_WhenRequestIsValid_ShouldCreateUser() throws Exception {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest("testuser", "test@example.com", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // Esperamos un 201 Created
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        // Assert 2
        var userInDb = userRepository.findByUsername("testuser");
        assertThat(userInDb).isPresent();
        assertThat(userInDb.get().getEmail()).isEqualTo("test@example.com");
        assertThat(userInDb.get().getPasswordHash()).isNotEqualTo("password123"); // Verificamos que la contraseña está hasheada
    }


    @Test
    void registerUser_WhenUsernameAlreadyExists_ShouldReturnConflict() throws Exception {

        userRepository.save(User.builder()
                .username("existinguser")
                .email("existing@example.com")
                .passwordHash("somehash")
                .build());


        RegisterUserRequest request = new RegisterUserRequest("existinguser", "new@example.com", "password123");


        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }


    @Test
    void registerUser_WhenRequestIsInvalid_ShouldReturnBadRequest() throws Exception {
        // En este test probamos las movidas guays del record RegisterUserRequest
        RegisterUserRequest request = new RegisterUserRequest("", "invalid@example.com", "password123");


        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("El nombre de usuario no puede estar vacío")); // Verificamos el mensaje de error para el campo 'username'
    }






}