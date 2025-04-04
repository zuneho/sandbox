package io.github.zuneho.mcp.service.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter // 직렬화를 위해 필요
public class AnthropicResponse {
    private String id;
    private String model;
    private List<ContentBlock> content;

    @Getter
    @Setter // 직렬화를 위해 필요
    public static class ContentBlock {
        private String type;
        private String text;
    }
}