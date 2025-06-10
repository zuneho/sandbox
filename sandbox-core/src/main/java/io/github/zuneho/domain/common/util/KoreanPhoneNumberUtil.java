package io.github.zuneho.domain.common.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class KoreanPhoneNumberUtil {
    private static final Pattern MOBILE_WITH_HYPHEN_PATTERN = Pattern.compile("^01[016789]-[0-9]{3,4}-[0-9]{4}$");
    private static final Pattern MOBILE_WITHOUT_HYPHEN_PATTERN = Pattern.compile("^01[016789][0-9]{7,8}$");

    private static final Pattern KOREAN_PHONE_WITH_HYPHEN_PATTERN = Pattern.compile(
            "^02-[0-9]{3,4}-[0-9]{4}$|" +                                   // 서울 (9~10자리)
                    "^0[3-9][0-9]-[0-9]{3,4}-[0-9]{4}$|" +                          // 기타 지역 (10~11자리)
                    "^1(544|566|577|588|599|600|644|661|666|670|688)-[0-9]{4}$"     // 전국대표번호 (8자리)
    );

    private static final Pattern KOREAN_PHONE_WITHOUT_HYPHEN_PATTERN = Pattern.compile(
            "^02[0-9]{7,8}$|" +                                             // 서울 (9~10자리)
                    "^0[3-9][0-9][0-9]{7,8}$|" +                                    // 기타 지역 (10~11자리)
                    "^1(544|566|577|588|599|600|644|661|666|670|688)[0-9]{4}$"      // 전국대표번호 (8자리)
    );

    public static boolean isValidKoreanPhoneNumber(String phoneNumber, boolean withHyphen) {
        return isValidKoreanMobile(phoneNumber, withHyphen) || isValidKoreanNoneMobile(phoneNumber, withHyphen);
    }

    public static boolean isValidKoreanNoneMobile(String phoneNumber, boolean withHyphen) {
        if (StringUtils.isBlank(phoneNumber)) {
            return false;
        }
        if (!withHyphen && phoneNumber.contains("-")) {
            return false;
        }
        return withHyphen ?
                KOREAN_PHONE_WITH_HYPHEN_PATTERN.matcher(phoneNumber).matches() :
                KOREAN_PHONE_WITHOUT_HYPHEN_PATTERN.matcher(phoneNumber).matches();
    }

    public static boolean isValidKoreanMobile(String phoneNumber, boolean withHyphen) {
        if (StringUtils.isBlank(phoneNumber)) {
            return false;
        }
        if (!withHyphen && phoneNumber.contains("-")) {
            return false;
        }
        return withHyphen ?
                MOBILE_WITH_HYPHEN_PATTERN.matcher(phoneNumber).matches() :
                MOBILE_WITHOUT_HYPHEN_PATTERN.matcher(phoneNumber).matches();
    }

    public static String toHyphenFormatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.contains("-")) {
            return phoneNumber;
        }
        if (!isValidKoreanPhoneNumber(phoneNumber, false)) {
            return phoneNumber;
        }

        int length = phoneNumber.length();
        if (phoneNumber.startsWith("1") && length == 8) { // 전국대표번호 (1xxx-xxxx)
            return phoneNumber.substring(0, 4) + "-" + phoneNumber.substring(4);
        }

        if (phoneNumber.startsWith("0") && length >= 9) { // 일반전화/휴대전화 (0으로 시작하는 모든 번호)
            int areaCodeLength = phoneNumber.startsWith("02") ? 2 : 3; // 서울은 지역번호 2자리, 나머지는 3자리
            int remainingDigits = length - areaCodeLength; // 남은 자릿수 계산 (지역번호 제외)
            if (remainingDigits == 7) { // 지역번호 + 3자리 + 4자리
                return phoneNumber.substring(0, areaCodeLength) + "-" +
                        phoneNumber.substring(areaCodeLength, areaCodeLength + 3) + "-" +
                        phoneNumber.substring(areaCodeLength + 3);
            } else if (remainingDigits == 8) { // 지역번호 + 4자리 + 4자리
                return phoneNumber.substring(0, areaCodeLength) + "-" +
                        phoneNumber.substring(areaCodeLength, areaCodeLength + 4) + "-" +
                        phoneNumber.substring(areaCodeLength + 4);
            }
        }
        return phoneNumber;
    }

    public static PhoneNumberType getPhoneNumberType(String phoneNumber) {
        if (StringUtils.isBlank(phoneNumber)) {
            return PhoneNumberType.INVALID;
        }

        String cleanNumber = phoneNumber.replace("-", "");

        if (MOBILE_WITHOUT_HYPHEN_PATTERN.matcher(cleanNumber).matches()) { // 휴대전화 패턴 확인
            return PhoneNumberType.MOBILE;
        }

        if (cleanNumber.startsWith("1") && cleanNumber.length() == 8) { // 전국대표번호 패턴 확인 (정확한 번호만)
            if (cleanNumber.matches("^1(544|566|577|588|599|600|644|661|666|670|688)[0-9]{4}$")) {
                return PhoneNumberType.NATIONAL;
            }
        }

        if (cleanNumber.startsWith("0") && cleanNumber.length() >= 9) { // 지역전화 패턴 확인 (정규식으로 정확히 검증)
            if (KOREAN_PHONE_WITHOUT_HYPHEN_PATTERN.matcher(cleanNumber).matches()) {
                return PhoneNumberType.LANDLINE;
            }
        }

        return PhoneNumberType.INVALID;
    }

    @Getter
    @RequiredArgsConstructor
    public enum PhoneNumberType {
        MOBILE("휴대전화"),           // 01x
        LANDLINE("지역전화"),         // 02, 03x-09x
        NATIONAL("전국대표번호"),     // 1544, 1566, 1577, 1588, 1599, 1600, 1644, 1661, 1666, 1670, 1688
        INVALID("유효하지않음");

        private final String description;
    }
}
