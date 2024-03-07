package com.experiments.service;

import com.experiments.domain.Experiment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
class ExperimentServiceTest {

    @MockBean
    private ReactiveRedisOperations<String, Experiment> reactiveRedisOperations;

    @Autowired
    private final ExperimentService experimentService = new ExperimentService(reactiveRedisOperations);

    @BeforeEach
    void setUp() {
        ReactiveValueOperations<String, Experiment> reactiveValueOps = Mockito.mock(ReactiveValueOperations.class);
        when(reactiveRedisOperations.opsForValue()).thenReturn(reactiveValueOps);
    }

    @Test
    void save() {


        Experiment experiment = new Experiment();
        experiment.setId("testId");
        when(reactiveRedisOperations.opsForValue().set(any(String.class), any(Experiment.class))).thenReturn(Mono.just(
                true));

        Mono<Experiment> result = experimentService.save(experiment.getId(), experiment);

        StepVerifier.create(result)
                .expectNext(experiment)
                .verifyComplete();
        Mockito.verify(reactiveRedisOperations.opsForValue()).set(eq("testId"), eq(experiment));


    }

    @Test
    void findById() {
        Experiment experiment = new Experiment();
        experiment.setId("testId");
        when(reactiveRedisOperations.opsForValue().get(any(String.class))).thenReturn(Mono.just(experiment));

        Mono<Experiment> result = experimentService.findById(experiment.getId());

        StepVerifier.create(result)
                .expectNext(experiment)
                .verifyComplete();
        Mockito.verify(reactiveRedisOperations.opsForValue()).get(eq("testId"));
    }

    @Test
    void findAll() {
        List<String> keys = List.of("key1", "key2");
        when(reactiveRedisOperations.keys("*")).thenReturn(Flux.fromIterable(keys));
        Experiment experiment1 = new Experiment();
        Experiment experiment2 = new Experiment();
        when(reactiveRedisOperations.opsForValue().get("key1")).thenReturn(Mono.just(experiment1));
        when(reactiveRedisOperations.opsForValue().get("key2")).thenReturn(Mono.just(experiment2));

        Flux<Experiment> result = experimentService.findAll();

        StepVerifier.create(result)
                .expectNext(experiment1, experiment2)
                .verifyComplete();
    }

    @Test
    void deleteById() {
        String key = "testId";
        when(reactiveRedisOperations.opsForValue().delete(key)).thenReturn(Mono.empty());

        Mono<Void> result = experimentService.deleteById(key);

        StepVerifier.create(result)
                .verifyComplete();
        Mockito.verify(reactiveRedisOperations.opsForValue()).delete(eq(key));
    }

    @Test
    void assignExperiment() {
        String userId = "testId";
        List<Experiment> experiments = List.of(new Experiment(), new Experiment());
        when(reactiveRedisOperations.opsForValue().set(any(String.class), any(Experiment.class))).thenReturn(Mono.just(
                true));

        int index = Math.floorMod(userId.hashCode(), experiments.size());
        Mono<Experiment> result = experimentService.assignExperiment(userId, experiments);
        StepVerifier.create(result)
                .expectNext(experiments.get(index))
                .verifyComplete();

    }
}
