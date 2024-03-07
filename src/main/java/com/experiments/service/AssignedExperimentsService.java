package com.experiments.service;

import com.experiments.domain.Experiment;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AssignedExperimentsService {
    private final ReactiveRedisOperations<String, Experiment> operations;

    public AssignedExperimentsService(ReactiveRedisOperations<String, Experiment> operations) {
        this.operations = operations;
    }

    public Mono<Experiment> save(String userId, Experiment experiment) {
        return operations.opsForValue().set(userId, experiment).thenReturn(experiment);
    }

    public Mono<Experiment> findById(String userId) {
        return operations.opsForValue().get(userId);
    }

    public Mono<Boolean> deleteById(String userId) {
        return operations.opsForValue().delete(userId);
    }
}
