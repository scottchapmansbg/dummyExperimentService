package com.experiments.service;

import java.util.Objects;

import com.experiments.domain.Experiment;
import com.experiments.domain.ExperimentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AssignedExperimentsService {
    private final ReactiveRedisOperations<String, Experiment> operations;

    private final TokenService tokenService;

    private final ExperimentAssignmentService experimentAssignmentService;

    private final LoggedOutExperimentAssignmentService loggedOutExperimentAssignmentService;

    private final ExperimentResponseService experimentResponseService;


    public AssignedExperimentsService(ReactiveRedisOperations<String, Experiment> operations,
                                      TokenService tokenService, CacheManager cacheManager, ExperimentAssignmentService experimentAssignmentService,
                                      LoggedOutExperimentAssignmentService loggedOutExperimentAssignmentService,
                                      ExperimentResponseService experimentResponseService
    ) {
        this.operations = operations;
        this.tokenService = tokenService;
        this.experimentAssignmentService = experimentAssignmentService;

        this.loggedOutExperimentAssignmentService = loggedOutExperimentAssignmentService;
        this.experimentResponseService = experimentResponseService;
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









    @Cacheable(value = "assignedExperiments", key = "#userId")
    public Mono<Experiment> assignExperimentToLoggedInUser(String userId, ServerWebExchange serverWebExchange) {
        log.info("Assigning experiment to user with id: {}", userId);
        Objects.requireNonNull(userId, "Id cannot be null");
        return tokenService.hasExperimentCookie(serverWebExchange.getRequest()).flatMap(hasCookie -> {
            Mono<String> tokenMono = hasCookie ? tokenService.getToken(serverWebExchange) : Mono.just(userId);
            return tokenMono.flatMap(experimentAssignmentService::getAssignedExperimentMono);
        });
    }

    public Mono<ExperimentResponse> assignExperimentToLoggedOutUser(ServerWebExchange serverWebExchange) {
        return tokenService.hasExperimentCookie(serverWebExchange.getRequest())
                .flatMap(hasCookie -> tokenService.getTokenOrGenerate(hasCookie, serverWebExchange))
                .flatMap(token ->
                        experimentAssignmentService.getAssignedExperimentMono(token).flatMap(
                                experiment -> {
                                    loggedOutExperimentAssignmentService.setExperimentAssignment(token, experiment.getId());
                                    return experimentResponseService.createExperimentResponse(Mono.just(experiment), token);
                                }
                        ));
    }

}
