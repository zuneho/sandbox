package io.github.zuneho.domain.common.converter;

import com.fasterxml.jackson.databind.util.StdConverter;
import io.github.zuneho.domain.common.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public class JsonLocalDateTimeDeserialize extends StdConverter<String, LocalDateTime> {

    @Override
    public LocalDateTime convert(String value) {
        return DateTimeUtil.convertLocalDateTime(value);
    }
}
