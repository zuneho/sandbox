package io.github.zuneho.mcp.service.model;

import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
public class WorkflowStatus {
    private final String sessionId;
    private final String status;
    private final Map<String, String> agentStatuses;

    @Builder
    public WorkflowStatus(String sessionId, String status, Map<String, String> agentStatuses) {
        this.sessionId = Objects.requireNonNull(sessionId, "Session ID must not be null");
        this.status = Objects.requireNonNull(status, "Status must not be null");
        this.agentStatuses = Objects.requireNonNullElseGet(agentStatuses, HashMap::new);
    }

    public void updateAgentStatus(String agentId, String status) {
        agentStatuses.put(agentId, status);
    }
}