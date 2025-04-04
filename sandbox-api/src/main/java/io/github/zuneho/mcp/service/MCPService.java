package io.github.zuneho.mcp.service;

import io.github.zuneho.config.AnthropicConfig;
import io.github.zuneho.config.MCPConfiguration;
import io.github.zuneho.config.MCPConfiguration.AgentConfig;
import io.github.zuneho.mcp.service.model.*;
import io.github.zuneho.mcp.service.model.ConversationSession.WorkflowStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MCPService {
    private static final Logger logger = LoggerFactory.getLogger(MCPService.class);

    private final MCPConfiguration mcpConfig;
    private final AnthropicConfig anthropicConfig;
    private final RestTemplate restTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    // 세션 저장소
    private final Map<String, ConversationSession> sessions = new ConcurrentHashMap<>();

    public MCPService(
            MCPConfiguration mcpConfig,
            AnthropicConfig anthropicConfig,
            RestTemplate restTemplate,
            SimpMessagingTemplate messagingTemplate) {
        this.mcpConfig = mcpConfig;
        this.anthropicConfig = anthropicConfig;
        this.restTemplate = restTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 채팅 메시지 처리 시작
     */
    public ConversationSession processMessage(ChatMessage chatMessage) {
        String sessionId = chatMessage.getSessionId();
        String userId = chatMessage.getUserId();

        // 세션 가져오기 또는 새로 생성
        ConversationSession session = getOrCreateSession(sessionId, userId);
        session.setStatus(WorkflowStatus.PROCESSING);

        // 사용자 메시지 추가
        session.addUserMessage(chatMessage.getContent());

        // 워크플로우 상태 알림
        sendWorkflowStatus(session);

        return session;
    }

    /**
     * 세션 가져오기 또는 새로 생성
     */
    public ConversationSession getOrCreateSession(String sessionId, String userId) {
        // 기존 세션이 있는 경우 반환
        if (sessionId != null && sessions.containsKey(sessionId)) {
            return sessions.get(sessionId);
        }

        // 새 세션 생성
        ConversationSession session = ConversationSession.builder()
                .userId(userId)
                .build();

        // 에이전트 목록 초기화
        List<String> agentIds = mcpConfig.getAgents().stream()
                .map(AgentConfig::getId)
                .collect(Collectors.toList());
        session.initializeAgentHistories(agentIds);

        // 세션 저장
        sessions.put(session.getSessionId(), session);

        return session;
    }

    /**
     * 세션 가져오기
     */
    public Optional<ConversationSession> getSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    /**
     * 워크플로우 실행
     */
    @Async
    public void executeWorkflow(ConversationSession session) {
        String sessionId = session.getSessionId();
        String userId = session.getUserId();

        try {
            // 워크플로우 시작
            sendWorkflowStatus(session);

            // 1단계: 번역기 에이전트 호출
            sendAgentStatus("translator", "processing", sessionId, userId);
            String translatedMessage = callAgent("translator", session,
                    getLastUserMessage(session));
            session.addAgentMessage("translator", translatedMessage);
            sendAgentResponse("translator", translatedMessage, "completed", sessionId, userId);

            // 2단계: 분석가 에이전트 호출
            sendAgentStatus("analyst", "processing", sessionId, userId);
            String analysis = callAgent("analyst", session,
                    "다음 내용을 분석해주세요: " + translatedMessage);
            session.addAgentMessage("analyst", analysis);
            sendAgentResponse("analyst", analysis, "completed", sessionId, userId);

            // 3단계: 요약가 에이전트 호출
            sendAgentStatus("summarizer", "processing", sessionId, userId);
            String summary = callAgent("summarizer", session,
                    "다음 분석 결과를 요약해주세요: " + analysis);
            session.addAgentMessage("summarizer", summary);
            sendAgentResponse("summarizer", summary, "completed", sessionId, userId);

            // 워크플로우 완료
            session.setStatus(WorkflowStatus.COMPLETED);
            sendWorkflowStatus(session);

            logger.info("워크플로우 완료: {}", sessionId);

        } catch (Exception e) {
            logger.error("워크플로우 실행 중 오류 발생: {}", e.getMessage(), e);
            session.setStatus(WorkflowStatus.IDLE);
            sendError(e.getMessage(), sessionId, userId);
        }
    }

    /**
     * 마지막 사용자 메시지 가져오기
     */
    private String getLastUserMessage(ConversationSession session) {
        List<Message> history = session.getConversationHistory();
        for (int i = history.size() - 1; i >= 0; i--) {
            Message message = history.get(i);
            if ("user".equals(message.getRole())) {
                return message.getContent();
            }
        }
        return "";
    }

    /**
     * 에이전트 호출
     */
    private String callAgent(String agentId, ConversationSession session, String message) throws Exception {
        // 에이전트 설정 찾기
        AgentConfig agentConfig = mcpConfig.getAgents().stream()
                .filter(config -> agentId.equals(config.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown agent: " + agentId));

        // 에이전트별 메시지 히스토리 가져오기
        List<Message> agentHistory = session.getAgentHistories().get(agentId);
        if (agentHistory == null) {
            agentHistory = new ArrayList<>();
            session.getAgentHistories().put(agentId, agentHistory);
        }

        // 현재 메시지 추가
        Message userMessage = Message.builder()
                .role("user")
                .content(message)
                .build();
        agentHistory.add(userMessage);

        // API 요청 생성
        AnthropicRequest request = AnthropicRequest.builder()
                .model(agentConfig.getModel())
                .messages(agentHistory)
                .system(agentConfig.getSystemPrompt())
                .build();

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", anthropicConfig.getApiKey());
        headers.set("anthropic-version", "2023-06-01");

        // HTTP 요청 실행
        HttpEntity<AnthropicRequest> entity = new HttpEntity<>(request, headers);
        AnthropicResponse response = restTemplate.postForObject(
                anthropicConfig.getApiUrl(), entity, AnthropicResponse.class);

        if (response == null || response.getContent() == null || response.getContent().isEmpty()) {
            throw new Exception("API 응답이 비어 있습니다");
        }

        // 응답 텍스트 추출
        String responseText = response.getContent().get(0).getText();

        // 에이전트 히스토리에 응답 추가
        Message assistantMessage = Message.builder()
                .role("assistant")
                .content(responseText)
                .build();
        agentHistory.add(assistantMessage);

        return responseText;
    }

    /**
     * 워크플로우 상태 전송
     */
    private void sendWorkflowStatus(ConversationSession session) {
        io.github.zuneho.mcp.service.model.WorkflowStatus status = io.github.zuneho.mcp.service.model.WorkflowStatus.builder()
                .sessionId(session.getSessionId())
                .status(session.getStatus().toString().toLowerCase())
                .build();

        // 각 에이전트 상태 설정
        session.getAgentHistories().forEach((agentId, history) -> {
            if (history.isEmpty()) {
                status.updateAgentStatus(agentId, "waiting");
            } else {
                status.updateAgentStatus(agentId, "completed");
            }
        });

        // 메시지 전송
        String destination = "/topic/status." + session.getSessionId();
        messagingTemplate.convertAndSend(destination, status);

        // 사용자별 메시지도 전송
        if (session.getUserId() != null) {
            messagingTemplate.convertAndSendToUser(session.getUserId(), "/queue/status", status);
        }
    }

    /**
     * 에이전트 상태 전송
     */
    private void sendAgentStatus(String agentId, String status, String sessionId, String userId) {
        Map<String, String> statusUpdate = new HashMap<>();
        statusUpdate.put("agentId", agentId);
        statusUpdate.put("status", status);

        // 세션 채널로 전송
        messagingTemplate.convertAndSend("/topic/agent-status." + sessionId, statusUpdate);

        // 사용자 채널로 전송
        if (userId != null && !userId.equals("anonymous")) {
            messagingTemplate.convertAndSendToUser(userId, "/queue/agent-status", statusUpdate);
        }
    }

    /**
     * 에이전트 응답 전송
     */
    private void sendAgentResponse(String agentId, String content, String status, String sessionId, String userId) {
        AgentResponse response = AgentResponse.builder()
                .agentId(agentId)
                .content(content)
                .status(status)
                .build();

        // 세션 채널로 전송
        messagingTemplate.convertAndSend("/topic/agent-response." + sessionId, response);

        // 사용자 채널로 전송
        if (userId != null && !userId.equals("anonymous")) {
            messagingTemplate.convertAndSendToUser(userId, "/queue/agent-response", response);
        }

        // 에이전트 상태 업데이트
        sendAgentStatus(agentId, status, sessionId, userId);
    }

    /**
     * 오류 메시지 전송
     */
    private void sendError(String errorMessage, String sessionId, String userId) {
        Map<String, String> error = new HashMap<>();
        error.put("type", "error");
        error.put("message", errorMessage);

        // 세션 채널로 전송
        messagingTemplate.convertAndSend("/topic/error." + sessionId, error);

        // 사용자 채널로 전송
        if (userId != null && !userId.equals("anonymous")) {
            messagingTemplate.convertAndSendToUser(userId, "/queue/error", error);
        }
    }
}