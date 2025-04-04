package io.github.zuneho.mcp.service.model;

import java.time.LocalDateTime;
import java.util.Objects;

import lombok.Builder;
import lombok.Getter;


@Getter
public class Message {
    private final String role;
    private final String content;
    private final String agentId;
    private final LocalDateTime timestamp;

    @Builder
    public Message(String role, String content, String agentId) {
        this.role = Objects.requireNonNull(role, "Role must not be null");
        this.content = Objects.requireNonNull(content, "Content must not be null");
        this.agentId = agentId; // 에이전트 ID는 null 가능 (사용자 메시지)
        this.timestamp = LocalDateTime.now();
    }
}