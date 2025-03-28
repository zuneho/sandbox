package io.github.zuneho.domain.common.util;

import org.apache.commons.lang3.StringUtils;

public class CustomStringUtil {

    public static String toLimitLength(String str, int maxLength) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }

    /**
     * 문자열 을 지정된 길이 만큼 만들고, 남은 공간은 공백 으로 채운다.
     * !!!!주의!!!! 가변폭 글꼴을 사용 하는 시스템 에서 해당 문자열 을 표현 하면 밀리는 현상이 발생할 수 있음. (비주얼 스튜디오 코드 같은 고정폭 글꼴 사용하는 시스템에서 문자열을 보면 올바르게 보임) UNICODE 적용 해도 동일한 문제 발생함.
     * @param text 원본 문자열
     * @param padLength 원하는 길이
     * @return 지정된 길이의 문자열 (부족한 부분은 공백 으로 채움, 초과 시 원본 반환)
     */
    public static String padString(String text, int padLength) {
        if (StringUtils.isEmpty(text)) {
            return StringUtils.EMPTY;
        }
        if (text.length() > padLength) {
            return text;
        }
        return String.format("%-" + padLength + "s", text);
    }

    public static String padNumber(Number number, int padLength){
        if(number == null){
            return StringUtils.EMPTY;
        }
        return padString(String.valueOf(number.longValue()), padLength);
    }
}
