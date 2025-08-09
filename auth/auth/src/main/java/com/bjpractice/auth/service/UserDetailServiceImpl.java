package com.bjpractice.auth.service;

import com.bjpractice.auth.client.UserClient;
import com.bjpractice.auth.model.CustomUserDetails;
import com.bjpractice.dtos.UserValidationResponse;
import feign.FeignException;
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

            return new CustomUserDetails(  // Instancia de nuestra clase custom
                    userResponse.id(),
                    userResponse.role(),
                    userResponse.username(),
                    userResponse.passwordHash()
            );
        } catch (FeignException.NotFound e) {
            // Si Feign nos da un 404, lo traducimos a una excepción que Spring Security entiende.
            throw new UsernameNotFoundException("Usuario no encontrado: " + username, e);
        }
    }


}
