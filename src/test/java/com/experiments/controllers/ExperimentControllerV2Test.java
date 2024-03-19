package com.experiments.controllers;

import java.time.LocalDateTime;
import java.util.List;

import com.experiments.domain.Experiment;
import com.experiments.domain.ExperimentResponse;
import com.experiments.service.AssignedExperimentsService;
import com.experiments.service.ExperimentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@WebFluxTest(controllers = ExperimentControllerV2.class)
@AutoConfigureWebTestClient
@ContextConfiguration(classes = ExperimentControllerV2.class)
class ExperimentControllerV2Test {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AssignedExperimentsService assignedExperimentsService;

    @MockBean
    private ExperimentService experimentService;

    private final String EXPERIMENT_URL = "/v2/experiment";

    private final LocalDateTime testDate = LocalDateTime.now();

    private final String userId = "1";

    private final List<Experiment> experimentList = List.of(
            new Experiment("1", testDate, "WebsiteColour", "Brown"),
            new Experiment("2", testDate, "WebsiteColour", "Blue"),
            new Experiment("3", testDate, "WebsiteColour", "Green")
    );

    @Test
    void createExperiment() {
        var experiment = new Experiment("1", testDate, "WebsiteColour", "Brown");

        var experimentFlux = Mono.just(experiment);
        when(experimentService.save(experiment.getId(), experiment)).thenReturn(experimentFlux);

        webTestClient
                .post()
                .uri(EXPERIMENT_URL)
                .bodyValue(experiment)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Experiment.class)
                .isEqualTo(experiment);
    }

    @Test
    void getAllExperiments() {
        when(experimentService.findAll()).thenReturn(Flux.just(
                new Experiment("1", testDate, "WebsiteColour", "Brown"),
                new Experiment("2", testDate, "WebsiteColour", "Blue"),
                new Experiment("3", testDate, "WebsiteColour", "Green")
        ));

        webTestClient
                .get()
                .uri(EXPERIMENT_URL)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Experiment.class)
                .hasSize(3);
    }

    @Test
    void assignUserToExperiment() {
        when(assignedExperimentsService.save(userId, new Experiment(userId, testDate, "WebsiteColour", "Brown"))).thenReturn(
                Mono.just(new Experiment(userId, testDate, "WebsiteColour", "Brown")));

        when(experimentService.findAll()).thenReturn(Flux.fromIterable(experimentList));

        when(experimentService.assignExperiment(userId)).thenReturn(Mono.just(new Experiment(userId, testDate, "WebsiteColour", "Brown")));

        String ASSIGN_URL = "/v2/assign";
        webTestClient
                .get()
                .uri(ASSIGN_URL + "?userId=" + userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ExperimentResponse.class)
                .consumeWith(response -> {
                    var responseBody = response.getResponseBody();
                    assert responseBody != null;
                    assert responseBody.getUserId().equals(userId);
                    assert experimentList.contains(responseBody.getExperiments());
                });
    }

    @Test
    void deleteExperiment() {
        var experimentId = "1";
        when(experimentService.deleteById(experimentId)).thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri(EXPERIMENT_URL + "/userId=" + experimentId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getAssignedExperiment() {
        when(assignedExperimentsService.findById(userId)).thenReturn(Mono.just(
                new Experiment(userId, testDate, "WebsiteColour", "Brown")
        ));

        webTestClient
                .get()
                .uri(EXPERIMENT_URL + "/{userId}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Experiment.class)
                .consumeWith(response -> {
                    var responseBody = response.getResponseBody();
                    assert responseBody != null;
                    assert responseBody.getId().equals(userId);
                });
    }

    @Test
    void deleteAssignedExperiment() {
        var experimentId = "1";
        when(assignedExperimentsService.deleteById(experimentId)).thenReturn(Mono.empty());

        String UNASSIGN_URL = "/v2/unassign";
        webTestClient
                .delete()
                .uri(UNASSIGN_URL + "/userId=" + experimentId)
                .exchange()
                .expectStatus().isNoContent();

    }
}
