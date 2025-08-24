package com.bjpractice.user.service;


import com.bjpractice.user.entity.User;
import com.bjpractice.user.exception.UserAlreadyExistsException;
import com.bjpractice.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.UnknownServiceException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }



    @Transactional
    public User registerUser(String email, String username, String password) {

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("El email '" + email + "' ya está registrado.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("El nombre de usuario '" + username + "' ya está en uso.");
        }


        String encodedPassword = passwordEncoder.encode(password);

        User newUser = User.builder()
                .username(username)
                .email(email)
                .passwordHash(encodedPassword)
                .build();

        return userRepository.save(newUser);

    }

    @Transactional
    public User updateBalance(Long userId, BigDecimal amountToAdd) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrao"));

        BigDecimal newBalance = user.getBalance().add(amountToAdd);
        user.setBalance(newBalance);

        return userRepository.save(user);

    }

    @Transactional(readOnly = true) // Solo lectura
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el nombre: " + username));

    }


}
