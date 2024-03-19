package com.experiments.controllers;

import com.experiments.service.LoginService;
import com.experiments.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

@Controller
public class LoginController {
    private final UserService userService;
    private final LoginService loginService;

    public LoginController(UserService userService, LoginService loginService) {
        this.userService = userService;
        this.loginService = loginService;
    }

    @GetMapping("/login{$userId}")
    public Mono<String> login(@PathVariable String userId) {
        return userService.logIn(userId).flatMap(isValid -> {
            if (isValid) {
                return Mono.just("Welcome " + userId);
            } else {
                return Mono.error(new RuntimeException("Invalid user"));
            }
        });
    }

    @GetMapping("/logout{$userId}")
    public Mono<String> logout(@PathVariable String userId) {
        return loginService.logout(userId).thenReturn("User " + userId + " logged out");
    }
}
