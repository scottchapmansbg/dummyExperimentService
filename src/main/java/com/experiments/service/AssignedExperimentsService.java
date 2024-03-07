package com.experiments.service;

import com.experiments.domain.Experiment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
@Slf4j
public class AssignedExperimentsService {
    private final ReactiveRedisOperations<String, Experiment> operations;

    public AssignedExperimentsService(ReactiveRedisOperations<String, Experiment> operations) {
        this.operations = operations;
    }

    public Mono<Experiment> save(String userId, Experiment experiment) {
        Objects.requireNonNull(userId, "Id cannot be null");
        Objects.requireNonNull(experiment, "Experiment cannot be null");
        return operations.opsForValue().set(userId, experiment).thenReturn(experiment)
                .doOnSuccess(experiment1 -> log.info("Experiment saved with id: " + userId))
                .doOnError(throwable -> log.error("Error saving experiment with id: " + userId))
                .onErrorResume(throwable -> Mono.empty());
    }

    public Mono<Experiment> findById(String userId) {
        Objects.requireNonNull(userId, "Id cannot be null");
        return operations.opsForValue().get(userId)
                .doOnSuccess(experiment -> log.info("Experiment found with id: " + userId))
                .doOnError(throwable -> log.error("Error finding experiment with id: " + userId))
                .onErrorResume(throwable -> Mono.empty());
    }

    public Mono<Boolean> deleteById(String userId) {
        Objects.requireNonNull(userId, "Id cannot be null");
        return operations.opsForValue().delete(userId)
                .doOnSuccess(aBoolean -> log.info("Experiment deleted with id: " + userId))
                .doOnError(throwable -> log.error("Error deleting experiment with id: " + userId))
                .onErrorResume(throwable -> Mono.just(false));
    }
}
