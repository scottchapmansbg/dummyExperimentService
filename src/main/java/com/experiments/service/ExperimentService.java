package com.experiments.service;

import java.math.BigInteger;
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

    private final TokenService tokenService;

    public ExperimentService(
            ReactiveRedisOperations<String, Experiment> operations,
            LoggedOutExperimentAssignmentService loggedOutExperimentAssignmentService,
            CaffeineCacheManager cacheManager,
            TokenService tokenService
    ) {
        this.operations = operations;
        this.loggedOutExperimentAssignmentService = loggedOutExperimentAssignmentService;
        this.experimentsCache = cacheManager.getCache("experiments");
        this.tokenService = tokenService;
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
        return Mono.justOrEmpty(experimentsCache.get(id, Experiment.class))
                .switchIfEmpty(operations.opsForValue().get(id))
                .switchIfEmpty(Mono.error(new NoExperimentsAvailableException("No experiments available")));
    }

    public Flux<Experiment> findAll() {
        log.info("Finding all experiments");
        return operations.keys("*").flatMap(operations.opsForValue()::get);
    }

    @NotNull
    public Mono<Void> deleteById(@NotNull String userId) {
        log.info("Deleting experiment with id: {}", userId);
        return operations.opsForValue().delete(userId).then();
    }
}
