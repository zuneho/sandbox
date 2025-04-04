package io.github.zuneho.config;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Getter
@Configuration
public class AnthropicConfig {

    private final String apiKey;
    private final String apiUrl;

    public AnthropicConfig(
            @Value("${anthropic.api.key}") String apiKey,
            @Value("${anthropic.api.url}") String apiUrl) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}