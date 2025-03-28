package io.github.zuneho.domain.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomStringUtilTest {

    @Test
    public void test_toLimitLength() {
        assertNull(CustomStringUtil.toLimitLength(null, 5));
        assertEquals("", CustomStringUtil.toLimitLength("", 5));

        String input = "Java";
        assertEquals(input, CustomStringUtil.toLimitLength(input, 5));

        input = "Hello";
        assertEquals(input, CustomStringUtil.toLimitLength(input, 5));

        input = "JavaProgramming";
        assertEquals("JavaP", CustomStringUtil.toLimitLength(input, 5));

        input = "こんにちは世界";
        assertEquals("こんにちは", CustomStringUtil.toLimitLength(input, 5));
    }

    @Test
    public void test_padString() {
        assertEquals("hello     ", CustomStringUtil.padString("hello", 10));
        assertEquals("hello", CustomStringUtil.padString("hello", 5)); //동일 길이 그대로 문자열 반환
        assertEquals("hello", CustomStringUtil.padString("hello", 0)); //길이를 초과 하기 때문에 문자열 그대로 반환
        assertEquals("long text", CustomStringUtil.padString("long text", 8)); // 원본 반환
        assertEquals("", CustomStringUtil.padString(null, 6)); // 공백
        assertEquals("", CustomStringUtil.padString("", 5));
    }
}