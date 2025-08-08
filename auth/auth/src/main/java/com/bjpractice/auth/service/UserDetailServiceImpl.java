package com.bjpractice.auth.service;

import com.bjpractice.auth.client.UserClient;
import com.bjpractice.auth.model.CustomUserDetails;
import com.bjpractice.dtos.UserValidationResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

//Esta clase es el traductor entre nuestro sistema de usuarios y Spring Security.
//Coge nuestro objeto UserValidationResponse y lo convierte al formato UserDetails estándar.
//Sin esta traducción, Spring Security no sabría cómo validar a nuestros usuarios.


@Service
public class UserDetailServiceImpl implements UserDetailsService {

    private final UserClient userClient;

    public UserDetailServiceImpl(UserClient userClient) {
        this.userClient = userClient;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            UserValidationResponse userResponse = userClient.findUserForValidation(username);

            // AHORA CREAMOS UNA INSTANCIA DE NUESTRA CLASE PERSONALIZADA
            return new CustomUserDetails(
                    userResponse.id(),           // 1. id
                    userResponse.role(),         // 2. role
                    userResponse.username(),     // 3. username
                    userResponse.passwordHash()  // 4. password
            );
        } catch (Exception e) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + username, e);
        }
    }


}
