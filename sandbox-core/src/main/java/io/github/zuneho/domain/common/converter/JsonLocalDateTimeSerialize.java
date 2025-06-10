package io.github.zuneho.domain.common.converter;

import com.fasterxml.jackson.databind.util.StdConverter;
import io.github.zuneho.domain.common.util.DateTimeUtil;

import java.time.LocalDateTime;

public class JsonLocalDateTimeSerialize extends StdConverter<LocalDateTime, String> {
    @Override
    public String convert(LocalDateTime value) {
        return DateTimeUtil.convertString(value);
    }
}
