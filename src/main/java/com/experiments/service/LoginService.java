package com.experiments.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class LoginService {
    private final Map<String,String> sessions;

    public LoginService() {
        this.sessions = new java.util.HashMap<>();
    }

    public Mono<Void> login(String userId) {
       sessions.put(userId, userId.hashCode() + System.currentTimeMillis() + "");
         return Mono.empty();
    }


    public Mono<Boolean> isLoggedIn(String userId, String token) {
        return Mono.just(sessions.get(userId).equals(token));
    }

    public Mono<Void> logout(String userId) {
        sessions.remove(userId);
        return Mono.empty();
    }
}
