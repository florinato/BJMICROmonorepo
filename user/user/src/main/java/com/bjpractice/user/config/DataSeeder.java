package com.bjpractice.user.config;

import com.bjpractice.user.service.UserService;
import org.springframework.boot.CommandLineRunner;

public class DataSeeder implements CommandLineRunner {

    private final UserService userService;


    public DataSeeder(UserService userService) {
        this.userService = userService;
    }

    // Este metodo se ejecuta cada vez que lanzemos el micro, pero como registerUser comprueba que el usuario existe o no, se detenderá
    @Override
    public void run(String... args) throws Exception {

        try {
            userService.registerUser("testuser", "test@example.com", "password123");
            System.out.println("Usuario de prueba creado exitosamente.");
        } catch (Exception e) {
            // Asumimos que si da error es porque ya existe, lo cual está bien
            System.out.println("El usuario de prueba ya existía.");
        }
    }
}
