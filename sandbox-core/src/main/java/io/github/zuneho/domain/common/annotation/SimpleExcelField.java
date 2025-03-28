package io.github.zuneho.domain.common.annotation;

import io.github.zuneho.domain.common.util.excel.SimpleExcelUtil;
import org.springframework.core.convert.converter.Converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleExcelField {
    String header() default "";

    int order() default 0;

    Class<? extends Converter<?, ?>> convert() default SimpleExcelUtil.NoneConverter.class;

    boolean nullable() default false;
}
