package com.experiments.service;

import org.apache.el.parser.Token;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LoggedOutExperimentAssignmentService {
    private final Map<String, String> experimentAssignments = new java.util.HashMap<>();
    private final Map<String, String> tokensToUserId = new java.util.HashMap<>();

    private String getExperimentAssignment(String userId) {
        return experimentAssignments.get(userId);
    }

    public void setExperimentAssignment(String token, String experimentId) {
        experimentAssignments.put(token, experimentId);
    }


}
