package com.bjpractice.auth.service;


import com.bjpractice.auth.client.UserClient;
import com.bjpractice.auth.dto.LoginRequest;
import com.bjpractice.auth.model.CustomUserDetails;
import com.bjpractice.dtos.UserValidationResponse;
import com.bjpractice.dtos.model.Role;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;



    public AuthenticationService(JwtService jwtService, AuthenticationManager authenticationManager) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;

    }

    public String login(LoginRequest request){
        // 1. Autenticamos. Si falla, lanza una excepción.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        // 2. Extraemos nuestro CustomUserDetails del objeto Authentication
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 3. Obtenemos los datos directamente de nuestro objeto
        Long userId = userDetails.getId();
        Role role = userDetails.getRole();

        // 4. Generamos el token. ¡No más llamadas extra a la red!
        return jwtService.generateToken(userId, role);
    }
}
