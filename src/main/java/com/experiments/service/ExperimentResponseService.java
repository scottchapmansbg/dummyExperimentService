package com.experiments.service;

import com.experiments.domain.Experiment;
import com.experiments.domain.ExperimentResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ExperimentResponseService {
    public Mono<ExperimentResponse> createExperimentResponse(Mono<Experiment> assignedExperimentMono, String token) {
        return assignedExperimentMono.flatMap(experiment -> {
//            loggedOutExperimentAssignmentService.setExperimentAssignment(token, experiment.getId());
            return Mono.just(new ExperimentResponse(experiment, token));
        });
    }
}

