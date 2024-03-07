package com.experiments.service;

import com.experiments.domain.Experiment;
import com.experiments.exceptionhandler.NoExperimentsAvailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ExperimentService {
    private final ReactiveRedisOperations<String, Experiment> operations;

    public ExperimentService(ReactiveRedisOperations<String, Experiment> operations) {
        this.operations = operations;
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

    public Mono<Experiment> assignExperiment(String userId, List<Experiment> experiments) {
        Objects.requireNonNull(userId, "Id cannot be null");
        Objects.requireNonNull(experiments, "Experiments cannot be null");
        log.info("Assigning experiment to user with id: {}", userId);
        int experimentNumber =Math.floorMod(userId.hashCode(), experiments.size());
        return Mono.just(experiments.get(experimentNumber));
    }
}
