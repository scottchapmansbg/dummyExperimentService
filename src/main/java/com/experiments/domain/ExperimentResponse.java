package com.experiments.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@JsonSerialize
@Getter
public class ExperimentResponse {
    @JsonProperty
    private Experiment experiments;
    @JsonProperty
    private String userId;
}
