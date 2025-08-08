package com.bjpractice.auth.service;


import com.bjpractice.auth.client.UserClient;
import com.bjpractice.dtos.UserValidationResponse;
import com.bjpractice.dtos.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserClient userClient;

    @InjectMocks
    private UserDetailServiceImpl userDetailService;

    private UserValidationResponse mockUserResponse;

    @BeforeEach
    void setUp() {
        // Preparamos los datos de ejemplo que nuestro mock devolverá.
        // Esto asegura que cada test empieza con un estado limpio y conocido.
        mockUserResponse = new UserValidationResponse(
                1L,
                "testuser",
                "$2a$10$abcdefghijklmnopqrstuv", // Un hash de Bcrypt simulado
                Role.USER
        );
    }

    @Test
    @DisplayName("Debe cargar UserDetails correctamente cuando el usuario existe")
    void shouldLoadUserByUsername_WhenUserExists() {
        // --- 1. ARRANGE (Preparar) ---
        // Configuramos el comportamiento de nuestro mock.
        // "Cuando se llame a userClient.findUserForValidation con 'testuser',
        // entonces devuelve nuestro objeto mockUserResponse".
        when(userClient.findUserForValidation("testuser")).thenReturn(mockUserResponse);

        // --- 2. ACT (Actuar) ---
        // Llamamos al método que queremos probar.
        UserDetails userDetails = userDetailService.loadUserByUsername("testuser");

        // --- 3. ASSERT (Verificar) ---
        // Comprobamos que el resultado es el esperado.
        assertNotNull(userDetails, "El UserDetails no debería ser nulo");
        assertEquals("testuser", userDetails.getUsername(), "El username no coincide");
        assertEquals("$2a$10$abcdefghijklmnopqrstuv", userDetails.getPassword(), "El password hash no coincide");

        // Verificamos que la autoridad se ha creado correctamente con el prefijo "ROLE_"
        assertTrue(userDetails.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER")),
                "El rol/autoridad no es correcto");
    }

    @Test
    @DisplayName("Debe lanzar UsernameNotFoundException cuando el usuario no existe")
    void shouldThrowUsernameNotFoundException_WhenUserDoesNotExist() {
        // --- 1. ARRANGE (Preparar) ---
        String nonExistentUsername = "unknownuser";

        // Configuramos el mock para que lance una excepción cuando se le pida este usuario.
        // Esto simula el caso en que el user-service devuelve un 404.
        when(userClient.findUserForValidation(nonExistentUsername))
                .thenThrow(new RuntimeException("Usuario no encontrado desde el mock"));

        // --- 2. ACT & 3. ASSERT (Actuar y Verificar) ---
        // Verificamos que al llamar al método, se lanza la excepción esperada.
        // La llamada al método se hace dentro de una expresión lambda.
        UsernameNotFoundException exceptionThrown = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailService.loadUserByUsername(nonExistentUsername),
                "Se esperaba que se lanzara UsernameNotFoundException"
        );

        // Opcional: Podemos verificar que el mensaje de la excepción es el que esperamos.
        assertTrue(exceptionThrown.getMessage().contains("Usuario no encontrado: " + nonExistentUsername));
    }



}
