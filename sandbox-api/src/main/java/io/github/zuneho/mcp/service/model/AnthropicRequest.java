package io.github.zuneho.mcp.service.model;

import java.util.List;
import java.util.Objects;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class AnthropicRequest {
    private final String model;
    private final List<Message> messages;
    private final String system;
    private final Integer maxTokens;

    private AnthropicRequest(String model, List<Message> messages, String system, Integer maxTokens) {
        this.model = Objects.requireNonNull(model, "Model must not be null");
        this.messages = Objects.requireNonNull(messages, "Messages must not be null");
        this.system = Objects.requireNonNull(system, "System prompt must not be null");
        this.maxTokens = Objects.requireNonNullElse(maxTokens, 1000);
    }
}