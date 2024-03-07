package com.experiments.service;

import com.experiments.domain.Experiment;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ExperimentService {
    private final ReactiveRedisOperations<String, Experiment> operations;

    public ExperimentService(ReactiveRedisOperations<String, Experiment> operations) {
        this.operations = operations;
    }

    public Mono<Experiment> save(String id, Experiment experiment) {
        return operations.opsForValue().set(id, experiment).thenReturn(experiment);
    }

    public Mono<Experiment> findById(String id) {
        return operations.opsForValue().get(id);
    }

    public Flux<Experiment> findAll() {
        return operations.keys("*")
                .flatMap(operations.opsForValue()::get);
    }

    public Mono<Void> deleteById(String userId) {
        return operations.opsForValue().delete(userId).then();
    }

    public Mono<Experiment> assignExperiment(String userId, List<Experiment> experiments) {
        int experimentNumber = userId.hashCode() % experiments.size();
        return Mono.just(experiments.get(experimentNumber));
    }
}
