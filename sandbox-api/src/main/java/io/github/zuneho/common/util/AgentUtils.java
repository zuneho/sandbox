package io.github.zuneho.common.util;

/**
 * 에이전트 관련 유틸리티 클래스
 */
public class AgentUtils {

    /**
     * 에이전트 ID를 표시 이름으로 변환
     */
    public static String getAgentDisplayName(String agentId) {
        return switch (agentId) {
            case "translator" -> "번역기";
            case "analyst" -> "분석가";
            case "summarizer" -> "요약가";
            default -> agentId;
        };
    }
}