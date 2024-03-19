package com.experiments.controllers;

import com.experiments.domain.Experiment;
import com.experiments.domain.ExperimentResponse;
import com.experiments.exceptionhandler.NoExperimentsAvailableException;
import com.experiments.service.AssignedExperimentsService;
import com.experiments.service.ExperimentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v2")
@Slf4j
public class ExperimentControllerV2 {

    private final ExperimentService experimentService;
    private final AssignedExperimentsService assignedExperimentsRepository;

    ExperimentControllerV2(
            AssignedExperimentsService assignedExperimentsRepository,
            ExperimentService experimentService
    ) {
        this.experimentService = experimentService;
        this.assignedExperimentsRepository = assignedExperimentsRepository;
    }

    @PostMapping("/experiment")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Experiment> createExperiment(@RequestBody @Valid Experiment experiment) {
        log.info("Creating experiment: {}", experiment);
        return experimentService.save(experiment.getId(), experiment);
    }

    @GetMapping("/experiment")
    @ResponseStatus(HttpStatus.OK)
    public Flux<Experiment> getAllExperiments() {
        log.info("Getting all experiments");
        return experimentService.findAll();
    }

    @GetMapping("/assign")
    @ResponseStatus(HttpStatus.OK)
    @CachePut(value = "assignedExperiments")
    public Mono<ExperimentResponse> assignUserToExperiment(@RequestParam @Valid String userId) {
        log.info("Assigning user {} to experiment", userId);
        return experimentService.assignExperimentToLoggedInUser(userId)
                .flatMap(experiment -> Mono.just(new ExperimentResponse(experiment, userId)))
                .onErrorResume(
                        NullPointerException.class,
                        e -> Mono.error(new NoExperimentsAvailableException("No experiments available"))
                );
    }

    @GetMapping("/assignLoggedOut")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ExperimentResponse> assignLoggedOutUserToExperiment(ServerWebExchange exchange) {
        log.info("Assigning logged out user to experiment");
        return experimentService.assignExperimentToLoggedOutUser(exchange);
    }

    @DeleteMapping("/experiment/{experimentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteExperiment(@PathVariable @Valid String experimentId) {
        log.info("Deleting experiment with id: {}", experimentId);
        return experimentService.deleteById(experimentId);
    }

    @GetMapping("/experiment/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Experiment> getAssignedExperiment(@Valid @PathVariable String userId) {
        log.info("Getting assigned experiment for user: {}", userId);
        return assignedExperimentsRepository.findById(userId);
    }

    @DeleteMapping("/unassign/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Boolean> deleteAssignedExperiment(@Valid @PathVariable String userId) {
        log.info("Deleting assigned experiment for user: {}", userId);
        return assignedExperimentsRepository.deleteById(userId);
    }

}
