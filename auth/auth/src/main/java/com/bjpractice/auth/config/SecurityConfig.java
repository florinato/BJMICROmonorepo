package com.bjpractice.auth.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Desactivamos CSRF porque nuestra API es stateless y no usa cookies de sesión.
                .csrf(AbstractHttpConfigurer::disable)

                // Definimos las reglas de autorización para las peticiones HTTP.
                .authorizeHttpRequests(auth -> auth
                        // Permitimos el acceso público a nuestro endpoint de login.
                        .requestMatchers("/api/auth/login").permitAll()
                        // Cualquier otra petición requiere autenticación.
                        .anyRequest().authenticated()
                )

                // Configuramos la gestión de sesiones para que sea STATELESS.
                // Spring no creará ni usará sesiones HTTP.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    // 2. El PasswordEncoder: Para hashear y verificar contraseñas.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 3. El AuthenticationManager: El gestor principal de la autenticación.
    // Lo exponemos como un Bean para poder inyectarlo en nuestro servicio de autenticación.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    }

