package io.github.zuneho.mcp.service.model;

import io.github.zuneho.common.util.AgentUtils;
import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Getter
public class AgentResponse {
    private final String agentId;
    private final String displayName;
    private final String content;
    private final String status;

    @Builder
    public AgentResponse(String agentId, String content, String status) {
        this.agentId = Objects.requireNonNull(agentId, "Agent ID must not be null");
        this.content = Objects.requireNonNull(content, "Content must not be null");
        this.status = Objects.requireNonNull(status, "Status must not be null");

        // 에이전트 ID를 표시 이름으로 변환
        this.displayName = AgentUtils.getAgentDisplayName(agentId);
    }
}