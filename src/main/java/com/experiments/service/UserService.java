package com.experiments.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final List<String> listOfUsers = List.of("User1", "User2", "User3");
    private final Map<String, String> userIdToToken = new java.util.HashMap<>();

    public Mono<Boolean> logIn(String userId) {
        return Mono.just(listOfUsers.contains(userId));
    }

    public Mono<Void> updateToken(String userId, String token) {
        userIdToToken.put(userId, token);
        return Mono.empty();
    }

    public Mono<String> getTokenFromUserId(String userId) {
        return Mono.just(userIdToToken.get(userId));
    }

    public Mono<String> createToken(String userId) {
        return Mono.just(userId.hashCode() + System.currentTimeMillis() + "");
    }


}
