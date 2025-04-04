package io.github.zuneho.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue"); // 메시지 브로커 설정: 서버->클라이언트 메시지 prefix
                config.setApplicationDestinationPrefixes("/app"); // 클라이언트->서버 메시지 prefix
                config.setUserDestinationPrefix("/user"); // 사용자별 구독 주소 prefix
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/mcp-websocket") // STOMP 엔드포인트 설정
                .withSockJS(); // SockJS 지원 추가
    }
}