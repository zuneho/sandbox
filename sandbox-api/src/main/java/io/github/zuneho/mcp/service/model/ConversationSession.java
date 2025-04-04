package io.github.zuneho.mcp.service.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Objects;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ConversationSession {
    private final String sessionId;
    private final String userId;
    private final LocalDateTime createdAt;
    private final List<Message> conversationHistory;
    private final Map<String, List<Message>> agentHistories;
    @Setter
    private WorkflowStatus status;

    @Builder
    public ConversationSession(String userId) {
        this.sessionId = UUID.randomUUID().toString();
        this.userId = Objects.requireNonNullElse(userId, "anonymous");
        this.createdAt = LocalDateTime.now();
        this.conversationHistory = new ArrayList<>();
        this.agentHistories = new HashMap<>();
        this.status = WorkflowStatus.IDLE;
    }

    public void initializeAgentHistories(List<String> agentIds) {
        agentIds.forEach(agentId -> agentHistories.put(agentId, new ArrayList<>()));
    }

    public void addUserMessage(String content) {
        Message message = Message.builder()
                .role("user")
                .content(content)
                .build();
        conversationHistory.add(message);
    }

    public void addAgentMessage(String agentId, String content) {
        Message message = Message.builder()
                .role("assistant")
                .content(content)
                .agentId(agentId)
                .build();
        conversationHistory.add(message);

        // 에이전트별 대화 기록에도 추가
        List<Message> agentHistory = agentHistories.get(agentId);
        if (agentHistory != null) {
            agentHistory.add(message);
        }
    }

    public enum WorkflowStatus {
        IDLE, PROCESSING, COMPLETED
    }
}