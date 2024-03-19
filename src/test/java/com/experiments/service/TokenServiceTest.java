package com.experiments.service;

import java.math.BigInteger;
import java.util.List;

import com.experiments.domain.Experiment;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;
    private final String tokenName = "experimentToken";

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();

    }

    @Test
    void getHash() {
        String token = "experimentToken";
        String expectedHash = DigestUtils.md5Hex(token).toUpperCase();

        Mono<String> actualHashMono = tokenService.getHash(token);

        StepVerifier.create(actualHashMono)
                .expectNext(expectedHash)
                .verifyComplete();
    }

    @Test
    void getIndexFromHash() {
        String tokenHash = "a1b2c3d4e5f6";
        List<Experiment> experimentList = List.of(new Experiment(), new Experiment(), new Experiment());
        int expectedIndex = new BigInteger(tokenHash, 16).mod(BigInteger.valueOf(experimentList.size())).intValueExact();

        StepVerifier.create(tokenService.getIndexFromHash(tokenHash, experimentList))
                .expectNext(expectedIndex)
                .verifyComplete();
    }

    @Test
    void hasExperimentCookieNoCookie() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));

        StepVerifier.create(tokenService.hasExperimentCookie(exchange.getRequest()))
                .expectNext(false)
                .verifyComplete();

    }

    @Test
    void hasExperimentCookieWithCookie() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/")
                        .cookie(ResponseCookie.from(tokenName, "experimentToken").build())
        );

        StepVerifier.create(tokenService.hasExperimentCookie(exchange.getRequest()))
                .expectNext(true)
                .verifyComplete();

    }

    @Test
    void generateToken() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));

        StepVerifier.create(tokenService.generateToken(exchange))
                .expectNextMatches(token -> !token.isEmpty())
                .verifyComplete();
    }

    @Test
    void getToken() {
        String expectedToken = "experimentToken";
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/")
                        .cookie(ResponseCookie.from(tokenName, expectedToken).build())
        );

        ResponseCookie cookie = ResponseCookie.from(tokenName, expectedToken).build();
        exchange.getResponse().addCookie(cookie);

        StepVerifier.create(tokenService.getToken(exchange))
                .expectNext(expectedToken)
                .verifyComplete();

    }
}
