package com.experiments.service;

import java.time.LocalDateTime;
import java.util.List;

import com.experiments.domain.Experiment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
class ExperimentServiceTest {
    @MockBean
    private CaffeineCacheManager caffeineCacheManager;

    @MockBean
    private ReactiveRedisOperations<String, Experiment> reactiveRedisOperations;

    @MockBean
    private LoggedOutExperimentAssignmentService loggedOutExperimentAssignmentService;

    private UserService userService;

    private ExperimentService experimentService;

    @BeforeEach
    void setUp() {
        ReactiveValueOperations<String, Experiment> reactiveValueOps = Mockito.mock(ReactiveValueOperations.class);
        when(reactiveRedisOperations.opsForValue()).thenReturn(reactiveValueOps);

        Cache cacheMock = Mockito.mock(Cache.class);
        when(caffeineCacheManager.getCache("experiments")).thenReturn(cacheMock);

        experimentService = new ExperimentService(
                reactiveRedisOperations,
                userService, loggedOutExperimentAssignmentService,
                caffeineCacheManager
        );
    }

    @Test
    void save() {

        Experiment experiment = new Experiment();
        experiment.setId("testId");
        when(reactiveRedisOperations.opsForValue().set(any(String.class), any(Experiment.class))).thenReturn(
                Mono.just(
                        true
                )
        );

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
        var cacheMock = Mockito.mock(Cache.class);
        when(caffeineCacheManager.getCache("assignedExperiments")).thenReturn(cacheMock);
        var experiments = List.of(
                new Experiment(
                        "testId1",
                        LocalDateTime.now(), "testName1", "testDescription1"
                ),
                new Experiment(
                        "testId2",
                        LocalDateTime.now(), "testName2", "testDescription2"
                )
        );

        when(reactiveRedisOperations.keys("*")).thenReturn(Flux.just("key1", "key2"));
        when(reactiveRedisOperations.opsForValue().get("key1")).thenReturn(Mono.just(experiments.get(0)));
        when(reactiveRedisOperations.opsForValue().get("key2")).thenReturn(Mono.just(experiments.get(1)));
        String userId = "testId";
        when(reactiveRedisOperations.opsForValue().set(any(String.class), any(Experiment.class))).thenReturn(
                Mono.just(
                        true
                )
        );

        int index = Math.floorMod(userId.hashCode(), experiments.size());
        Mono<Experiment> result = experimentService.assignExperimentToLoggedInUser(userId);
        StepVerifier.create(result)
                .consumeNextWith(
                        experiment -> {
                            assert experiment != null;
                            assert experiments.contains(experiment);
                        }
                )
                .verifyComplete();

    }
}
