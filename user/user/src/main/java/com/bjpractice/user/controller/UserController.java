package com.bjpractice.user.controller;


import com.bjpractice.user.dto.RegisterUserRequest;
import com.bjpractice.user.dto.UserResponse;
import com.bjpractice.user.entity.User;
import com.bjpractice.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody @Valid RegisterUserRequest request){

        User newUser = userService.registerUser(
                request.email(),
                request.username(),
                request.password()
        );

        UserResponse response = new UserResponse(
                newUser.getId(),
                newUser.getUsername(),
                newUser.getEmail(),
                newUser.getBalance(),
                newUser.getRole().name()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

}
