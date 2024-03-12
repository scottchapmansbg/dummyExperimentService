package com.experiments.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class Experiment {

    @NotNull
    @JsonProperty
    @Getter
    private String id;

    @NotNull
    @JsonProperty
    private LocalDateTime createdAt;

    @NotNull
    @JsonProperty
    private String name;

    @NotNull
    @JsonProperty
    private String type;
}
