package io.github.zuneho.domain.common.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class NumberUtilTest {

    @Test
    public void testIsPositiveInteger() {
        assertTrue(NumberUtil.isPositiveInteger(10), "10 should be positive");
        assertTrue(NumberUtil.isPositiveInteger(100L), "100L should be positive");
        assertFalse(NumberUtil.isPositiveInteger(-1), "-1 should not be positive");
        assertFalse(NumberUtil.isPositiveInteger(0), "0 should not be positive");
        assertFalse(NumberUtil.isPositiveInteger(null), "null should not be positive");
        assertFalse(NumberUtil.isPositiveInteger(-100L), "-100L should not be positive");
    }

    @Test
    void testIsGreaterThanZero() {
        // Given
        BigDecimal positiveValue = new BigDecimal("5.00");
        BigDecimal zeroValue = BigDecimal.ZERO;
        BigDecimal negativeValue = new BigDecimal("-5.00");

        // When & Then
        assertTrue(NumberUtil.isGreaterThanZero(positiveValue));
        assertFalse(NumberUtil.isGreaterThanZero(zeroValue));
        assertFalse(NumberUtil.isGreaterThanZero(negativeValue));
    }

    @Test
    void testIsGreaterThan() {
        // Given
        BigDecimal value1 = new BigDecimal("10.00");
        BigDecimal value2 = new BigDecimal("5.00");

        // When & Then
        assertTrue(NumberUtil.isGreaterThan(value1, value2));
        assertFalse(NumberUtil.isGreaterThan(value2, value1));
        assertFalse(NumberUtil.isGreaterThan(value1, value1));
    }

    @Test
    void testIsLessThan() {
        // Given
        BigDecimal value1 = new BigDecimal("5.00");
        BigDecimal value2 = new BigDecimal("10.00");

        // When & Then
        assertTrue(NumberUtil.isLessThan(value1, value2));
        assertFalse(NumberUtil.isLessThan(value2, value1));
        assertFalse(NumberUtil.isLessThan(value1, value1));
    }

    @Test
    void testIsGreaterOrEquals() {
        // Given
        BigDecimal value1 = new BigDecimal("10.00");
        BigDecimal value2 = new BigDecimal("10.00");
        BigDecimal value3 = new BigDecimal("5.00");

        // When & Then
        assertTrue(NumberUtil.isGreaterOrEquals(value1, value2));
        assertTrue(NumberUtil.isGreaterOrEquals(value1, value3));
        assertFalse(NumberUtil.isGreaterOrEquals(value3, value1));
    }

    @Test
    void testNullValues() {
        // Given
        BigDecimal value2 = new BigDecimal("10.00");

        // When & Then
        assertFalse(NumberUtil.isGreaterThan(null, value2));
        assertFalse(NumberUtil.isLessThan(value2, null));
        assertFalse(NumberUtil.isGreaterOrEquals(null, value2));
    }

    @Test
    void testIsNumberText(){
        assertFalse(NumberUtil.isNumberText(null));
        assertFalse(NumberUtil.isNumberText("1234null"));
        assertFalse(NumberUtil.isNumberText(" 12"));
        assertFalse(NumberUtil.isNumberText(" 12 "));
        assertFalse(NumberUtil.isNumberText("12.23"));


        assertTrue(NumberUtil.isNumberText("12345"));
    }
}