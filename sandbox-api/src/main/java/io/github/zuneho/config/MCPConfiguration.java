package io.github.zuneho.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mcp")
public class MCPConfiguration {

    private List<AgentConfig> agents;

    @Setter
    @Getter
    public static class AgentConfig {
        private String id;

        private String model;

        private String systemPrompt;
    }
}