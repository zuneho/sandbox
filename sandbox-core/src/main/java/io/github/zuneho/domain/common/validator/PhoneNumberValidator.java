package io.github.zuneho.domain.common.validator;

import io.github.zuneho.domain.common.annotation.PhoneNumber;
import io.github.zuneho.domain.common.util.KoreanPhoneNumberUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    private boolean nullable;
    private boolean withHyphen;
    private boolean mobileOnly;

    @Override
    public void initialize(PhoneNumber phoneNumber) {
        ConstraintValidator.super.initialize(phoneNumber);
        this.withHyphen = phoneNumber.withHyphen();
        this.nullable = phoneNumber.nullable();
        this.mobileOnly = phoneNumber.mobileOnly();
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (this.nullable && StringUtils.isBlank(phoneNumber)) {
            return true;
        }
        if (mobileOnly) {
            return KoreanPhoneNumberUtil.isValidKoreanMobile(phoneNumber, this.withHyphen);
        }
        return KoreanPhoneNumberUtil.isValidKoreanPhoneNumber(phoneNumber, this.withHyphen);
    }
}
