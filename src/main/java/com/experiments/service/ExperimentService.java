package com.experiments.service;

import java.util.Objects;

import com.experiments.domain.Experiment;
import com.experiments.exceptionhandler.NoExperimentsAvailableException;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ExperimentService {
    private final ReactiveRedisOperations<String, Experiment> operations;

    private final Cache experimentsCache;

    public ExperimentService(ReactiveRedisOperations<String, Experiment> operations, CaffeineCacheManager cacheManager) {
        this.operations = operations;
        this.experimentsCache = cacheManager.getCache("experiments");
    }

    public Mono<Experiment> save(String id, Experiment experiment) {
        Objects.requireNonNull(id, "Id cannot be null");
        Objects.requireNonNull(experiment, "Experiment cannot be null");
        log.info("Saving experiment with id: {}", id);
        return operations.opsForValue().set(id, experiment).thenReturn(experiment);
    }

    public Mono<Experiment> findById(String id) {
        Objects.requireNonNull(id, "Id cannot be null");
        log.info("Finding experiment with id: {}", id);
        return operations.opsForValue().get(id).switchIfEmpty(Mono.error(new NoExperimentsAvailableException("Experiment not found")));
    }

    public Flux<Experiment> findAll() {
        log.info("Finding all experiments");
        return operations.keys("*")
                .flatMap(operations.opsForValue()::get);
    }

    public Mono<Void> deleteById(String userId) {
        Objects.requireNonNull(userId, "Id cannot be null");
        log.info("Deleting experiment with id: {}", userId);
        return operations.opsForValue().delete(userId).then();
    }

    @Cacheable(value = "experiments", key = "#userId")
    public Mono<Experiment> assignExperiment(String userId) {
        log.info("Assigning experiment to user with id: {}", userId);
        Objects.requireNonNull(userId, "Id cannot be null");

        return Mono.justOrEmpty(experimentsCache.get(userId, Experiment.class))
                .switchIfEmpty(findAll().collectList().flatMap(experimentList -> {
                    log.info("Assigned Experiment not in cache. Retrieving from db");
                    if (experimentList.isEmpty()) {
                        return Mono.error(new NoExperimentsAvailableException("No experiments available"));
                    }
                    int index = Math.floorMod(userId.hashCode(), experimentList.size());
                    Experiment experiment = experimentList.get(index);
                    experimentsCache.put(userId, experiment);
                    return Mono.just(experiment);
                })).log();
    }
}
