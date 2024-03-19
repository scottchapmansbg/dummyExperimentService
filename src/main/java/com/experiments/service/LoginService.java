package com.experiments.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class LoginService {
    private final Map<String, String> sessions;

    public LoginService() {
        this.sessions = new java.util.HashMap<>();
    }

    public Mono<Void> logout(String userId) {
        sessions.remove(userId);
        return Mono.empty();
    }
}
