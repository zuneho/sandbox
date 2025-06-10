package io.github.zuneho.domain.common.converter;

import io.github.zuneho.domain.common.util.DateTimeUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CustomStringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

    @Override
    public LocalDateTime convert(@NonNull String source) {
        return DateTimeUtil.convertLocalDateTime(source);
    }
}
