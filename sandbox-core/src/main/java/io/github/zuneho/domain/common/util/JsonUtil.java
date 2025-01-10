package io.github.zuneho.domain.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Slf4j
public class JsonUtil {
    private JsonUtil() {
    }


    @Getter
    private static final ObjectMapper defaultMapper;

    static {
        defaultMapper = new ObjectMapper();
        defaultMapper.registerModule(new JavaTimeModule());
        defaultMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static String toJsonStringIgnoreError(Object obj) {
        try {
            return toJsonStringWithThrows(obj);
        } catch (JsonProcessingException e) {
            log.warn("json converting fail objectName[{}]", Objects.nonNull(obj) ? obj.getClass().getName() : null, e);
            return "{}";
        }
    }

    public static String toJsonStringWithThrows(Object obj) throws JsonProcessingException {
        return defaultMapper.writeValueAsString(obj);
    }


    public static Map<String, String> toMap(Object object) {
        try {
            Map<String, Object> rawMap = defaultMapper.convertValue(object, Map.class);

            Map<String, String> stringMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
                stringMap.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
            }
            return stringMap;
        } catch (Exception e) {
            log.error("Failed to convert object to Map<String, String>", e);
            throw new RuntimeException("Failed to convert object to Map<String, String>");
        }
    }
}
