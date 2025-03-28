package io.github.zuneho.domain.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DateTimeUtil {
    private static final LocalTime PUSH_REJECT_NIGHT_START = LocalTime.of(21, 0); // 오후 9시
    private static final LocalTime PUSH_REJECT_MORNING_START = LocalTime.of(8, 0); // 오전 8시

    public static final String FORMAT_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String FORMAT_YYYY_MM_DD = "yyyy-MM-dd";

    private static final DateTimeFormatter INT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private static final List<DateTimeFormatter> FORMATTERS = List.of(
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ofPattern(FORMAT_YYYY_MM_DD_HH_MM_SS),
            DateTimeFormatter.ofPattern(FORMAT_YYYY_MM_DD_HH_MM)
    );

    public static LocalDateTime convertLocalDateTime(String formatedString) {
        if (StringUtils.isBlank(formatedString)) {
            return null;
        }

        if (formatedString.length() == 10) {
            try {
                LocalDate date = LocalDate.parse(formatedString, DateTimeFormatter.ofPattern(FORMAT_YYYY_MM_DD));
                return date.atStartOfDay();
            } catch (Exception e) {
                log.warn("convertLocalDateTime invalid input: {}", formatedString);
                return null;
            }
        }

        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDateTime.parse(formatedString, formatter);
            } catch (DateTimeParseException ignored) { //무시하고 다음으로
            }
        }
        log.warn("convertLocalDateTime invalid input: {}", formatedString);
        return null;
    }

    public static String convertString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.format(DateTimeFormatter.ofPattern(FORMAT_YYYY_MM_DD_HH_MM_SS));
    }


    public static String toIntDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(INT_DATE_TIME_FORMATTER);
    }

    public static boolean isWithinRange(LocalDateTime now, LocalTime baseTime, int gapMinutes) {
        LocalTime nowTime = now.toLocalTime();
        // 시작 시간과 종료 시간 계산
        LocalTime startTime = baseTime.minusMinutes(gapMinutes);
        LocalTime endTime = baseTime.plusMinutes(gapMinutes);

        // 현재 시간이 범위 내에 있는지 확인
        return !nowTime.isBefore(startTime) && !nowTime.isAfter(endTime);
    }

    public static LocalDateTime adjustToEndOfDayIfMidnight(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        if (dateTime.toLocalTime().equals(LocalTime.MIDNIGHT) || dateTime.toLocalTime().equals(LocalTime.MIN)) {
            return dateTime.with(LocalTime.of(23, 59, 59));
        }
        return dateTime;
    }

    public static boolean isMinuteSuffixMatching(LocalDateTime dateTime, int suffix) {
        if (suffix < 0 || suffix > 9) {
            throw new IllegalArgumentException("Minute unit must be between 0 and 9.");
        }
        return dateTime.getMinute() % 10 == suffix;
    }


    /**
     * 현재 시간이 법적으로 푸쉬 금지시간인 야간 시간(21:00 ~ 08:00)일 경우 가장 빠른 오전 8시를 반환, 아닐 경우 현재 시간을 반환하는 메서드
     */
    public static LocalDateTime getNearPushEnableTime(LocalDateTime now) {
        LocalTime currentTime = now.toLocalTime();

        if (currentTime.isAfter(PUSH_REJECT_NIGHT_START) || currentTime.isBefore(PUSH_REJECT_MORNING_START)) {    // 야간 시간인 경우 다음 날 오전 8시로 조정
            long addDay = currentTime.isBefore(PUSH_REJECT_MORNING_START) && currentTime.isAfter(LocalTime.MIDNIGHT)
                    ? 0L
                    : 1L;
            return now.toLocalDate().plusDays(addDay).atTime(PUSH_REJECT_MORNING_START);
        }
        return now;
    }
}
