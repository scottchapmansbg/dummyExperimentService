package com.experiments.controllers;

import java.util.HashMap;

import com.experiments.domain.Experiment;
import com.experiments.domain.ExperimentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1")
public class ExperimentControllerV1 {
    private final HashMap<String, Experiment> experimentCache = new HashMap<>();

    @PostMapping("/experiment")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Experiment> createExperiment(@RequestBody @Valid Experiment experiment) {
        experimentCache.put(experiment.getId(), experiment);
        return Mono.just(experiment);
    }

    @GetMapping("/experiment")
    @ResponseStatus(HttpStatus.OK)
    public Flux<Experiment> getAllExperiments() {
        return Flux.fromIterable(experimentCache.values());
    }

    @GetMapping("/assign")
    @ResponseBody
    public Mono<ExperimentResponse> AssignUserToExperiment(@RequestParam String userId) {
        int experimentNumber = userId.hashCode() % experimentCache.size();
        var experimentList = experimentCache.values().stream().toList();
        return Mono.just(new ExperimentResponse(experimentList.get(experimentNumber), userId));
    }

    @DeleteMapping("/experiment")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> DeleteExperiment(@RequestParam String userId) {
        if (experimentCache.isEmpty()) {
            return Mono.error(new Throwable("No experiments set"));
        }

        experimentCache.remove(userId);
        return Mono.empty();
    }
}
