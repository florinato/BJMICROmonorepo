package com.bjpractice.auth.client;

import com.bjpractice.dtos.UserValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${application.config.user-service-url}")
public interface UserClient {

    @GetMapping("/api/v1/users/internal/validate/{username}")
    UserValidationResponse findUserForValidation(@PathVariable("username") String username);

}