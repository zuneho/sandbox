package io.github.zuneho.domain.common.converter;

import io.github.zuneho.domain.common.util.DateTimeUtil;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;

public class LocalDateTimeToStringConverter implements Converter<LocalDateTime, String> {

    @Override
    public String convert(LocalDateTime source) {
        return DateTimeUtil.convertString(source);
    }
}
