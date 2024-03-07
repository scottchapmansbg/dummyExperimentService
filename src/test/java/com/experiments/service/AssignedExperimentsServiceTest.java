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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class AssignedExperimentsServiceTest {

    @MockBean
    private ReactiveRedisOperations<String, Experiment> reactiveRedisOperations;

    @Autowired
    private AssignedExperimentsService assignedExperimentsService;

    @BeforeEach
    void setUp() {
        ReactiveValueOperations<String, Experiment> valueOperationsMock = Mockito.mock(ReactiveValueOperations.class);
        when(reactiveRedisOperations.opsForValue()).thenReturn(valueOperationsMock);
        assignedExperimentsService = new AssignedExperimentsService(reactiveRedisOperations);
    }

    @Test
    void save() {
        Experiment experiment = new Experiment();
        String userId = "testId";
        when(reactiveRedisOperations.opsForValue().set(any(String.class), any(Experiment.class))).thenReturn(Mono.just(
                true));

        Mono<Experiment> result = assignedExperimentsService.save(userId, experiment);

        StepVerifier.create(result)
                .expectNext(experiment)
                .verifyComplete();
    }

    @Test
    void findById() {
        Experiment experiment = new Experiment();
        String userId = "testId";
        when(reactiveRedisOperations.opsForValue().get(any(String.class))).thenReturn(Mono.just(experiment));

        Mono<Experiment> result = assignedExperimentsService.findById(userId);

        StepVerifier.create(result)
                .expectNext(experiment)
                .verifyComplete();
    }

    @Test
    void deleteById() {
        String userId = "testId";
        when(reactiveRedisOperations.opsForValue().delete(any(String.class))).thenReturn(Mono.just(true));

        Mono<Boolean> result = assignedExperimentsService.deleteById(userId);

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }
}
