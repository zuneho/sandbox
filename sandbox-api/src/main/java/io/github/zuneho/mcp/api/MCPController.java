package io.github.zuneho.mcp.api;

import io.github.zuneho.config.MCPConfiguration;
import io.github.zuneho.mcp.service.MCPService;
import io.github.zuneho.mcp.service.model.ChatMessage;
import io.github.zuneho.mcp.service.model.ConversationSession;
import io.github.zuneho.common.util.AgentUtils;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class MCPController {

    private final MCPService mcpService;
    private final MCPConfiguration mcpConfig;

    public MCPController(MCPService mcpService, MCPConfiguration mcpConfig) {
        this.mcpService = mcpService;
        this.mcpConfig = mcpConfig;
    }

    /**
     * 메인 페이지
     */
    @GetMapping("/")
    public String index(Model model) {
        // 에이전트 정보를 모델에 추가
        model.addAttribute("agents", mcpConfig.getAgents().stream()
                .map(agent -> {
                    Map<String, String> agentInfo = new HashMap<>();
                    agentInfo.put("id", agent.getId());
                    agentInfo.put("displayName", AgentUtils.getAgentDisplayName(agent.getId()));
                    agentInfo.put("description", agent.getSystemPrompt());
                    return agentInfo;
                })
                .collect(Collectors.toList()));

        return "index";
    }

    /**
     * 세션 페이지 (특정 세션에 접속)
     */
    @GetMapping("/session/{sessionId}")
    public String session(@PathVariable String sessionId, Model model) {
        // 세션 정보 가져오기
        ConversationSession session = mcpService.getSession(sessionId)
                .orElseGet(() -> ConversationSession.builder().userId("anonymous").build());

        model.addAttribute("sessionId", session.getSessionId());
        model.addAttribute("conversationHistory", session.getConversationHistory());
        model.addAttribute("agents", mcpConfig.getAgents());

        return "session";
    }

    /**
     * 에이전트 목록 조회 API
     */
    @GetMapping("/api/agents")
    @ResponseBody
    public ResponseEntity<?> getAgents() {
        return ResponseEntity.ok(mcpConfig.getAgents().stream()
                .map(agent -> {
                    Map<String, String> agentInfo = new HashMap<>();
                    agentInfo.put("id", agent.getId());
                    agentInfo.put("displayName", AgentUtils.getAgentDisplayName(agent.getId()));
                    agentInfo.put("description", agent.getSystemPrompt());
                    return agentInfo;
                })
                .collect(Collectors.toList()));
    }

    /**
     * 새 세션 생성 API
     */
    @PostMapping("/api/sessions")
    @ResponseBody
    public ResponseEntity<?> createSession() {
        ConversationSession session = mcpService.getOrCreateSession(null, "anonymous");
        Map<String, String> response = new HashMap<>();
        response.put("sessionId", session.getSessionId());
        return ResponseEntity.ok(response);
    }

    /**
     * 세션 정보 조회 API
     */
    @GetMapping("/api/sessions/{sessionId}")
    @ResponseBody
    public ResponseEntity<?> getSession(@PathVariable String sessionId) {
        return mcpService.getSession(sessionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 채팅 메시지 전송 API
     */
    @PostMapping("/api/chat")
    @ResponseBody
    public ResponseEntity<?> sendMessage(@RequestBody ChatMessage chatMessage) {
        ConversationSession session = mcpService.processMessage(chatMessage);

        // 워크플로우 비동기 실행 시작
        mcpService.executeWorkflow(session);

        Map<String, String> response = new HashMap<>();
        response.put("sessionId", session.getSessionId());
        response.put("status", "processing");
        return ResponseEntity.ok(response);
    }

    /**
     * WebSocket을 통한 채팅 메시지 처리
     */
    @MessageMapping("/chat")
    public void handleWebSocketMessage(@Payload ChatMessage chatMessage,
                                       Principal principal) {
        // 사용자 정보 설정
        if (principal != null) {
            chatMessage.setUserId(principal.getName());
        } else if (chatMessage.getUserId() == null) {
            chatMessage.setUserId("anonymous");
        }

        // 메시지 처리
        ConversationSession session = mcpService.processMessage(chatMessage);

        // 비동기 워크플로우 실행
        mcpService.executeWorkflow(session);
    }
}