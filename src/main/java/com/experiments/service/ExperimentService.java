package com.experiments.service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

import com.experiments.domain.Experiment;
import com.experiments.domain.ExperimentResponse;
import com.experiments.exceptionhandler.NoExperimentsAvailableException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ExperimentService {
    private final ReactiveRedisOperations<String, Experiment> operations;

    private final LoggedOutExperimentAssignmentService loggedOutExperimentAssignmentService;
    private final Cache experimentsCache;

    public ExperimentService(
            ReactiveRedisOperations<String, Experiment> operations,
            UserService userService, LoggedOutExperimentAssignmentService loggedOutExperimentAssignmentService,
            CaffeineCacheManager cacheManager
    ) {
        this.operations = operations;
        this.loggedOutExperimentAssignmentService = loggedOutExperimentAssignmentService;
        this.experimentsCache = cacheManager.getCache("experiments");
    }

    public Mono<Experiment> save(String id, Experiment experiment) {
        Objects.requireNonNull(id, "Id cannot be null");
        Objects.requireNonNull(experiment, "Experiment cannot be null");
        log.info("Saving experiment with id: {}", id);
        return operations.opsForValue().set(id, experiment).thenReturn(experiment);
    }

    @Cacheable(value = "experiments", key = "#id")
    public Mono<Experiment> findById(String id) {
        Objects.requireNonNull(id, "Id cannot be null");
        log.info("Finding experiment with id: {}", id);
        return Mono.justOrEmpty(
                experimentsCache.get(id, Experiment.class)
        ).switchIfEmpty(operations.opsForValue().get(id))
                .switchIfEmpty(Mono.error(new NoExperimentsAvailableException("No experiments available")));
    }

    public Flux<Experiment> findAll() {
        log.info("Finding all experiments");
        return operations.keys("*")
                .flatMap(operations.opsForValue()::get);
    }

    @NotNull
    public Mono<Void> deleteById(@NotNull String userId) {
        log.info("Deleting experiment with id: {}", userId);
        return operations.opsForValue().delete(userId).then();
    }

    @Cacheable(value = "assignedExperiments", key = "#userId")
    public Mono<Experiment> assignExperimentToLoggedInUser(String userId) {
        log.info("Assigning experiment to user with id: {}", userId);
        Objects.requireNonNull(userId, "Id cannot be null");

        return getExperimentMono(userId);
    }

    @Cacheable(value = "assignedExperiments", key = "#userId")
    public Mono<ExperimentResponse> assignExperimentToLoggedOutUser(ServerWebExchange webExchange) {
        ServerHttpRequest request = webExchange.getRequest();
        ServerHttpResponse response = webExchange.getResponse();
        String token;
        if (request.getCookies().get("experimentToken") != null) {
            token = request.getCookies().get("experimentToken").get(0).getValue();
        } else {
            token = String.valueOf(request.getId().hashCode() + System.currentTimeMillis());
        }

        response.addCookie(ResponseCookie.from("experimentToken", token).build());
        return getExperimentMono(token)
                .flatMap(experiment -> {
                    loggedOutExperimentAssignmentService.setExperimentAssignment(token, experiment.getId());
                    return Mono.just(new ExperimentResponse(experiment, token));
                });
    }

    @NotNull
    private Mono<Experiment> getExperimentMono(String token) {
        return Mono.justOrEmpty(experimentsCache.get(token, Experiment.class))
                .switchIfEmpty(findAll().collectList().flatMap(experimentList -> {
                    log.info("Assigned Experiment not in cache. Retrieving from db");
                    if (experimentList.isEmpty()) {
                        return Mono.error(new NoExperimentsAvailableException("No experiments available"));
                    }

                    var hash = getHash(token);
                    return hash.flatMap(
                            h -> {
                                log.info("Hash: {}", h);
                                BigInteger index = new BigInteger(
                                        h,
                                        16
                                ).mod(new BigInteger(String.valueOf(experimentList.size())));
                                log.info("Index: {}", index.intValueExact());

                                Experiment experiment = experimentList.get(index.intValueExact());
                                experimentsCache.put(token, experiment);
                                return Mono.just(experiment);
                            }
                    );

                })
                );
    }

    private Mono<String> getHash(String token) {
        var encodedHash = DigestUtils.md5Hex(token);
        return Mono.just(encodedHash.toUpperCase());
    }

    private Mono<Experiment> hashToExperimentNumber(String tokenHash, List<Experiment> experimentList) {
        BigInteger index = new BigInteger(tokenHash, 16).mod(new BigInteger(tokenHash));
        Experiment experiment = experimentList.get(index.intValueExact());
        experimentsCache.put(tokenHash, experiment);
        return Mono.just(experiment);
    }

}
