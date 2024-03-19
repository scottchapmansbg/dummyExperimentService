package com.experiments.service;

import com.experiments.domain.Experiment;
import com.experiments.exceptionhandler.NoExperimentsAvailableException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ExperimentAssignementService {

    private final Cache experimentsCache;
    private final ExperimentService experimentService;

    private final TokenService tokenService;

    public ExperimentAssignementService(CacheManager cacheManager, ExperimentService experimentService,
                                        TokenService tokenService
    ) {
        this.experimentsCache = cacheManager.getCache("experiments");
        this.experimentService = experimentService;
        this.tokenService = tokenService;
    }

    @NotNull
    public Mono<Experiment> getAssignedExperimentMono(String token) {
        return Mono.justOrEmpty(experimentsCache.get(token, Experiment.class))
                .switchIfEmpty(experimentService.findAll().collectList().flatMap(experimentList -> {
                    log.info("Assigned Experiment not in cache. Retrieving from db");
                    if (experimentList.isEmpty()) {
                        return Mono.error(new NoExperimentsAvailableException("No experiments available"));
                    }
                    return tokenService.getHash(token)
                            .flatMap(h -> tokenService.getIndexFromHash(h, experimentList).flatMap(index -> {
                                Experiment experiment = experimentList.get(index);
                                experimentsCache.put(token, experiment);
                                return Mono.just(experiment);
                            }));
                }));
    }
}
