package io.github.zuneho.domain.common.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;


@UtilityClass
public class NumberUtil {

    /**
     * 정수가 아닌 자료형 일 경우 false !
     */
    public static boolean isPositiveInteger(Number number) {
        return switch (number) {
            case null -> false;
            case Long l -> number.longValue() > 0;
            case Integer i -> number.intValue() > 0;
            default -> false;
        };
    }

    /*
        BigDecimal compare rule
        number1.compareTo(number2)
        number1 이 number2 보다 크면 1
        number1 이 number2 보다 작으면 -1
        number1 이 number2 보다 같으면 0
     */

    public static boolean isGreaterThanZero(BigDecimal bigDecimal) {
        return isGreaterThan(bigDecimal, BigDecimal.ZERO);
    }

    /**
     * @param source  기준값
     * @param compare 비교값
     * @return ex)
     * 기준값 > 비교값 = true
     * 기준값 >= 비교값 = false
     * 기준값 < 비교값 = false
     */
    public static boolean isGreaterThan(BigDecimal source, BigDecimal compare) {
        if (source == null || compare == null) {
            return false;
        }
        return source.compareTo(compare) > 0;
    }


    /**
     * @param source  기준값
     * @param compare 비교값
     * @return ex)
     * 기준값 > 비교값 = true
     * 기준값 >= 비교값 = true
     * 기준값 < 비교값 = false
     */
    public static boolean isGreaterOrEquals(BigDecimal source, BigDecimal compare) {
        if (source == null || compare == null) {
            return false;
        }
        return source.compareTo(compare) >= 0;
    }

    public static boolean isLessThan(BigDecimal source, BigDecimal compare) {
        if (source == null || compare == null) {
            return false;
        }
        return source.compareTo(compare) < 0;
    }


    public static BigDecimal divideToIntegerWithCeil(BigDecimal dividend, BigDecimal divisor) {
        if (dividend == null || divisor == null) {
            return BigDecimal.ZERO;
        }
        return dividend.divide(divisor, 0, RoundingMode.CEILING);
    }

    public static String numberWithComma(Number number) {
        if (number == null) {
            return null;
        }
        return new DecimalFormat("#,###").format(number);
    }


    public boolean isNumberText(String text){
        return StringUtils.isNotBlank(text) && text.matches("\\d+");
    }
}
