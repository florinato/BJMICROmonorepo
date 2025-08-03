package com.bjpractice.user.config;

import com.bjpractice.user.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
//
//@Component
//public class DataSeeder implements CommandLineRunner {
//
//    private final UserService userService;
//
//    public DataSeeder(UserService userService) {
//        this.userService = userService;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        try {
//
//            userService.registerUser("test@example.com", "testuser", "password123");
//            System.out.println("Usuario de prueba creado exitosamente.");
//        } catch (Exception e) {
//            System.out.println("El usuario de prueba ya existía o hubo un error al crearlo.");
//        }
//    }
//}