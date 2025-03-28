package io.github.zuneho.domain.common.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilTest {
    @Test
    void adjustToEndOfDayIfMidnight() {
        // Case 1: 0시 0분일 경우 -> 23시 59분 59초로 변경
        LocalDateTime midnight = LocalDateTime.of(2024, 12, 29, 0, 0);
        LocalDateTime adjustedMidnight = DateTimeUtil.adjustToEndOfDayIfMidnight(midnight);
        assertEquals(LocalDateTime.of(2024, 12, 29, 23, 59, 59), adjustedMidnight);

        // Case 2: 10시 30분일 경우 -> 원래 값 반환
        LocalDateTime otherTime = LocalDateTime.of(2024, 12, 29, 10, 30);
        LocalDateTime adjustedOtherTime = DateTimeUtil.adjustToEndOfDayIfMidnight(otherTime);
        assertEquals(otherTime, adjustedOtherTime);

        // Case 3: 0시 1분일 경우 -> 원래 값 반환
        LocalDateTime nearMidnight = LocalDateTime.of(2024, 12, 29, 0, 1);
        LocalDateTime adjustedNearMidnight = DateTimeUtil.adjustToEndOfDayIfMidnight(nearMidnight);
        assertEquals(nearMidnight, adjustedNearMidnight);

        // Case 4: 23시 59분 59초일 경우 -> 원래 값 반환
        LocalDateTime endOfDay = LocalDateTime.of(2024, 12, 29, 23, 59, 59);
        LocalDateTime adjustedEndOfDay = DateTimeUtil.adjustToEndOfDayIfMidnight(endOfDay);
        assertEquals(endOfDay, adjustedEndOfDay);

        //Case 5: null 일 경우 null 반환
        assertNull(DateTimeUtil.adjustToEndOfDayIfMidnight(null));
    }

    @Test
    void testIsWithinRange() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 8, 12, 4);

        assertTrue(DateTimeUtil.isWithinRange(now, LocalTime.of(12, 3), 1));
        assertTrue(DateTimeUtil.isWithinRange(now, LocalTime.of(12, 4), 0));
        assertTrue(DateTimeUtil.isWithinRange(now, LocalTime.of(12, 8), 4));


        assertTrue(DateTimeUtil.isWithinRange(now, LocalTime.of(12, 5), 1));
        assertFalse(DateTimeUtil.isWithinRange(now, LocalTime.of(12, 8), 3));
        assertFalse(DateTimeUtil.isWithinRange(now, LocalTime.of(12, 9), 4));
    }

    @Test
    void testConvertLocalDateTime_ValidFormats() {
        // Valid date-time strings
        assertEquals(LocalDateTime.of(2024, 12, 11, 12, 34, 56),
                DateTimeUtil.convertLocalDateTime("2024-12-11 12:34:56"));
        assertEquals(LocalDateTime.of(2024, 12, 11, 12, 34),
                DateTimeUtil.convertLocalDateTime("2024-12-11 12:34"));
        assertEquals(LocalDateTime.of(2024, 12, 11, 0, 0),
                DateTimeUtil.convertLocalDateTime("2024-12-11"));
        assertEquals(LocalDateTime.of(2024, 12, 11, 12, 34, 56),
                DateTimeUtil.convertLocalDateTime("2024-12-11T12:34:56"));
    }

    @Test
    void testConvertLocalDateTime_InvalidFormats() {
        // Invalid date-time strings
        assertNull(DateTimeUtil.convertLocalDateTime("invalid-date"));
        assertNull(DateTimeUtil.convertLocalDateTime("2024/12/11 12:34:56"));
        assertNull(DateTimeUtil.convertLocalDateTime(""));
        assertNull(DateTimeUtil.convertLocalDateTime(null));
    }

    @Test
    void testConvertLocalDateTime_BoundaryCases() {
        // Edge cases
        assertNull(DateTimeUtil.convertLocalDateTime("2024-12-32")); // Invalid day
        assertNull(DateTimeUtil.convertLocalDateTime("2024-13-11")); // Invalid month
        assertNull(DateTimeUtil.convertLocalDateTime("0000-00-00")); // Invalid date
    }

    @Test
    void testConvertString_ValidInput() {
        // Valid LocalDateTime input
        LocalDateTime dateTime = LocalDateTime.of(2024, 12, 11, 12, 34, 56);
        assertEquals("2024-12-11 12:34:56", DateTimeUtil.convertString(dateTime));
    }

    @Test
    void testConvertString_NullInput() {
        // Null input
        assertNull(DateTimeUtil.convertString(null));
    }

    @Test
    void testConvertLocalDateTimeToStringAndBack() {
        // Conversion from LocalDateTime to String and back
        LocalDateTime dateTime = LocalDateTime.of(2024, 12, 11, 12, 34, 56);
        String dateTimeString = DateTimeUtil.convertString(dateTime);
        LocalDateTime convertedDateTime = DateTimeUtil.convertLocalDateTime(dateTimeString);

        assertEquals(dateTime, convertedDateTime);
    }


    @Test
    void test_getNearPushEnableTime() {
        // Case 1: 오후 9시 이후 (야간 시간)
        LocalDateTime testTime = LocalDateTime.of(2023, 10, 10, 21, 30); // 21:30
        LocalDateTime expected = LocalDateTime.of(2023, 10, 11, 8, 0);  // 다음 날 08:00
        assertEquals(expected, DateTimeUtil.getNearPushEnableTime(testTime), "Failed for 21:30");

        // Case 2: 오전 8시 이전 (야간 시간)
        testTime = LocalDateTime.of(2023, 10, 11, 7, 0); // 07:00
        expected = LocalDateTime.of(2023, 10, 11, 8, 0); // 같은 날 08:00
        assertEquals(expected, DateTimeUtil.getNearPushEnableTime(testTime), "Failed for 07:00");

        // Case 3: 오전 8시 이후 (야간 시간 아님)
        testTime = LocalDateTime.of(2023, 10, 11, 8, 1); // 08:01
        expected = testTime; // 그대로 반환
        assertEquals(expected, DateTimeUtil.getNearPushEnableTime(testTime), "Failed for 08:01");

        // Case 4: 오후 8시 59분 (야간 시간 아님)
        testTime = LocalDateTime.of(2023, 10, 10, 20, 59); // 20:59
        expected = testTime; // 그대로 반환
        assertEquals(expected, DateTimeUtil.getNearPushEnableTime(testTime), "Failed for 20:59");

        // Case 5: 정확히 오후 9시 (야간 시간)
        testTime = LocalDateTime.of(2023, 10, 10, 21, 0); // 21:00
        expected = LocalDateTime.of(2023, 10, 10, 21, 0);  //  동일 시간
        assertEquals(expected, DateTimeUtil.getNearPushEnableTime(testTime), "Failed for 21:00");

        // Case 5-1: 정확히 오후 9시 1초(야간 시간)
        testTime = LocalDateTime.of(2023, 10, 10, 21, 0, 1); // 21:00
        expected = LocalDateTime.of(2023, 10, 11, 8, 0);  // 다음 날 08:00
        assertEquals(expected, DateTimeUtil.getNearPushEnableTime(testTime), "Failed for 21:00");

        // Case 6: 정확히 오전 8시 (야간 시간 아님)
        testTime = LocalDateTime.of(2023, 10, 11, 8, 0); // 08:00
        expected = testTime; // 그대로 반환
        assertEquals(expected, DateTimeUtil.getNearPushEnableTime(testTime), "Failed for 08:00");

        // Case 7: 오전 6시 59분 59초 (야간 시간)
        testTime = LocalDateTime.of(2023, 10, 11, 6, 59, 59); // 06:59:59
        expected = LocalDateTime.of(2023, 10, 11, 8, 0);      // 같은 날 08:00
        assertEquals(expected, DateTimeUtil.getNearPushEnableTime(testTime), "Failed for 06:59:59");
    }


    @Test
    void testIsMinuteSuffixMatching() {
        // **Test Case 1: Current Time**
        LocalDateTime now = LocalDateTime.of(2025, 1, 21, 14, 19);
        assertTrue(DateTimeUtil.isMinuteSuffixMatching(now, 9), "Expected true because the minute ends with 9");

        // **Test Case 2: Specific Time (Not Matching)**
        now = LocalDateTime.of(2025, 1, 21, 14, 23);
        assertFalse(DateTimeUtil.isMinuteSuffixMatching(now, 9), "Expected false because the minute does not end with 9");

        // **Test Case 3: Edge Case (Minute Ends With 0)**
        now = LocalDateTime.of(2025, 1, 21, 14, 0);
        assertTrue(DateTimeUtil.isMinuteSuffixMatching(now, 0), "Expected true because the minute ends with 0");

        // **Test Case 4: Edge Case (Minute Ends With 9)**
        now = LocalDateTime.of(2025, 1, 21, 14, 59);
        assertTrue(DateTimeUtil.isMinuteSuffixMatching(now, 9), "Expected true because the minute ends with 9");

        // **Test Case 5: Invalid Input (Negative n)**
        now = LocalDateTime.of(2025, 1, 21, 14, 15);
        LocalDateTime finalNow = now;
        assertThrows(IllegalArgumentException.class,
                () -> DateTimeUtil.isMinuteSuffixMatching(finalNow, -1),
                "Expected an exception for negative minute unit");

        // **Test Case 6: Invalid Input (n > 9)**
        assertThrows(IllegalArgumentException.class,
                () -> DateTimeUtil.isMinuteSuffixMatching(finalNow, 10),
                "Expected an exception for minute unit greater than 9");
    }
}