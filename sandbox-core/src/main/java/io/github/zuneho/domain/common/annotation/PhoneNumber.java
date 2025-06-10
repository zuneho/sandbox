package io.github.zuneho.domain.common.annotation;

import io.github.zuneho.domain.common.validator.PhoneNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {PhoneNumberValidator.class})
public @interface PhoneNumber {
    String message() default "올바른 전화 번호를 확인할 수 없습니다.";

    boolean nullable() default false;

    boolean withHyphen() default false;

    boolean mobileOnly() default false;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
