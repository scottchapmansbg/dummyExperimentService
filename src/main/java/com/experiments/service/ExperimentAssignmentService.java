package com.experiments.service;

import com.experiments.domain.Experiment;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
public class ExperimentAssignmentService {

    //TODO: Add error handling
    private final Cache experimentsCache;
    private final ExperimentService experimentService;

    private final TokenService tokenService;

    public ExperimentAssignmentService(CacheManager cacheManager, ExperimentService experimentService,
                                       TokenService tokenService
    ) {
        this.experimentsCache = cacheManager.getCache("experiments");
        this.experimentService = experimentService;
        this.tokenService = tokenService;
    }

    @NotNull
    public Mono<Experiment> getAssignedExperimentMono(String token) {
        return Mono.justOrEmpty(experimentsCache.get(token, Experiment.class))
                .switchIfEmpty(fetchExperimentsThenAssign(token));
    }

    private Mono<Experiment> fetchExperimentsThenAssign(String token) {
        return experimentService.findAll().collectList()
                .flatMap(experimentList -> {
                    if (experimentList.isEmpty()) {
                        return Mono.empty();
                    }
                    return assignExperimentFromList(token, experimentList);
                });
    }

    private Mono<Experiment> assignExperimentFromList(String token, List<Experiment> experimentList) {
        return tokenService.getHash(token)
                .flatMap(tokenHash -> tokenService.getIndexFromHash(tokenHash, experimentList)
                        .flatMap(index -> {
                            Experiment experiment = experimentList.get(index);
                            experimentsCache.put(token, experiment);
                            return Mono.just(experiment);
                        }));

    }

}
