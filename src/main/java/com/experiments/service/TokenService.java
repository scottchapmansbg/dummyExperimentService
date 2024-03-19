package com.experiments.service;

import java.math.BigInteger;
import java.util.List;

import com.experiments.domain.Experiment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class TokenService {
    public Mono<String> getHash(String token) {
        var encodedHash = DigestUtils.md5Hex(token);
        return Mono.just(encodedHash.toUpperCase());
    }

    public Mono<Integer> getIndexFromHash(String tokenHash, List<Experiment> experimentList) {
        log.info("Hash: {}", tokenHash);
        return Mono.just(
                new BigInteger(
                        tokenHash,
                        16
                ).mod(new BigInteger(String.valueOf(experimentList.size()))).intValueExact()
        );
    }

    public Mono<Boolean> hasExperimentCookie(ServerHttpRequest request) {
        return Mono.just(request.getCookies().get("experimentToken") != null);
    }

    public Mono<String> generateToken(ServerWebExchange serverWebExchange) {
        String token = String.valueOf(serverWebExchange.getRequest().getId().hashCode() + System.currentTimeMillis());
        serverWebExchange.getResponse().addCookie(ResponseCookie.from("experimentToken", token).build());
        return Mono.just(token);
    }

    public Mono<String> getToken(ServerWebExchange serverWebExchange) {
        return Mono.just(serverWebExchange.getRequest().getCookies().get("experimentToken").get(0).getValue());
    }

    public Mono<String> getTokenOrGenerate(Boolean hasCookie, ServerWebExchange serverWebExchange) {
        return hasCookie ? getToken(serverWebExchange) : generateToken(serverWebExchange);
    }
}
