package io.github.zuneho.mcp.service.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
public class ChatMessage {
    private final String content;
    private final String sessionId;
    @Setter
    private String userId; // 인증과 관련된 필드는 수정 가능하도록 설정

    @Builder
    public ChatMessage(String content, String sessionId, String userId) {
        this.content = Objects.requireNonNull(content, "Content must not be null");
        this.sessionId = sessionId; // 세션 ID는 null 가능 (새 세션 생성 시)
        this.userId = Objects.requireNonNullElse(userId, "anonymous");
    }
}