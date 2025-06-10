package io.github.zuneho.domain.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class KoreanPhoneNumberUtilTest {

    @Nested
    @DisplayName("전체 전화번호 검증 테스트")
    class OverallPhoneValidationTest {

        @ParameterizedTest
        @ValueSource(strings = {
                "010-1234-5678", "010-123-4567", "011-1234-5678",
                "016-123-4567", "017-1234-5678", "018-123-4567", "019-1234-5678"
        })
        @DisplayName("유효한 휴대전화번호 (하이픈 포함)")
        void validMobileWithHyphen(String phoneNumber) {
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanPhoneNumber(phoneNumber, true));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "01012345678", "0101234567", "01112345678",
                "01612345678", "0171234567", "01812345678", "01912345678"
        })
        @DisplayName("유효한 휴대전화번호 (하이픈 없음)")
        void validMobileWithoutHyphen(String phoneNumber) {
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanPhoneNumber(phoneNumber, false));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "02-123-4567", "02-1234-5678",
                "031-123-4567", "031-1234-5678", "032-123-4567",
                "033-1234-5678", "041-123-4567", "042-1234-5678",
                "043-123-4567", "044-1234-5678", "051-123-4567",
                "052-1234-5678", "053-123-4567", "054-1234-5678",
                "055-123-4567", "061-1234-5678", "062-123-4567",
                "063-1234-5678", "064-123-4567",
                "070-4123-4567", "070-7456-7890", "070-8999-1234",
                "050-1234-5678", "060-123-4567", "080-123-4567"
        })
        @DisplayName("유효한 비휴대전화번호 (하이픈 포함)")
        void validNonMobileWithHyphen(String phoneNumber) {
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanPhoneNumber(phoneNumber, true));
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanNoneMobile(phoneNumber, true));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "021234567", "0212345678", "0311234567", "03112345678",
                "0321234567", "0331234567", "04112345678", "0421234567",
                "04312345678", "0441234567", "05112345678", "0521234567",
                "05312345678", "0541234567", "05512345678", "0611234567",
                "06212345678", "0631234567", "06412345678",
                "07041234567", "07074567890", "07089991234",
                "05012345678", "0601234567", "0801234567"
        })
        @DisplayName("유효한 비휴대전화번호 (하이픈 없음)")
        void validNonMobileWithoutHyphen(String phoneNumber) {
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanPhoneNumber(phoneNumber, false));
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanNoneMobile(phoneNumber, false));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "1544-1234", "1566-5678", "1577-9999", "1588-0000",
                "1599-1111", "1600-2222", "1644-3333", "1661-4444",
                "1666-5555", "1670-6666", "1688-7777"
        })
        @DisplayName("유효한 전국대표번호 (하이픈 포함)")
        void validNationalWithHyphen(String phoneNumber) {
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanPhoneNumber(phoneNumber, true));
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanNoneMobile(phoneNumber, true));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "15441234", "15665678", "15779999", "15880000",
                "15991111", "16002222", "16443333", "16614444",
                "16665555", "16706666", "16887777"
        })
        @DisplayName("유효한 전국대표번호 (하이픈 없음)")
        void validNationalWithoutHyphen(String phoneNumber) {
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanPhoneNumber(phoneNumber, false));
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanNoneMobile(phoneNumber, false));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "012-1234-5678", "013-1234-5678", "015-1234-5678",
                "020-1234-5678", "010-12345-678", "010-12-34567",
                "1500-1234", "1545-5678", "1567-9999", "1700-1234",
                "123-456-7890", "invalid-number", ""
        })
        @DisplayName("유효하지 않은 전화번호")
        void invalidPhoneNumbers(String phoneNumber) {
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanPhoneNumber(phoneNumber, true));
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanPhoneNumber(phoneNumber, false));
        }

        @Test
        @DisplayName("null 처리")
        void nullPhoneNumber() {
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanPhoneNumber(null, true));
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanPhoneNumber(null, false));
        }

        @Test
        @DisplayName("하이픈 불일치 케이스")
        void hyphenMismatchCase() {
            // withHyphen=false인데 하이픈이 포함된 경우
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanPhoneNumber("010-1234-5678", false));
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanPhoneNumber("02-123-4567", false));
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanPhoneNumber("1588-1234", false));
        }
    }

    @Nested
    @DisplayName("휴대전화번호 검증 테스트")
    class MobilePhoneValidationTest {

        @ParameterizedTest
        @ValueSource(strings = {
                "010-1234-5678", "010-123-4567", "011-1234-5678",
                "016-123-4567", "017-1234-5678", "018-123-4567", "019-1234-5678"
        })
        @DisplayName("유효한 휴대전화번호 (하이픈 포함)")
        void validMobileWithHyphen(String phoneNumber) {
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanMobile(phoneNumber, true));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "01012345678", "0101234567", "01112345678",
                "01612345678", "0171234567", "01812345678", "01912345678"
        })
        @DisplayName("유효한 휴대전화번호 (하이픈 없음)")
        void validMobileWithoutHyphen(String phoneNumber) {
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanMobile(phoneNumber, false));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "012-1234-5678", "013-1234-5678", "015-1234-5678",
                "020-1234-5678", "02-123-4567", "031-123-4567"
        })
        @DisplayName("유효하지 않은 휴대전화번호")
        void invalidMobile(String phoneNumber) {
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanMobile(phoneNumber, true));
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanMobile(phoneNumber, false));
        }
    }

    @Nested
    @DisplayName("비휴대전화번호 검증 테스트")
    class NonMobilePhoneValidationTest {

        @ParameterizedTest
        @ValueSource(strings = {
                "02-123-4567", "02-1234-5678",
                "031-123-4567", "031-1234-5678", "070-4123-4567",
                "050-1234-5678", "060-123-4567", "080-123-4567"
        })
        @DisplayName("유효한 지역전화번호 (하이픈 포함)")
        void validLandlineWithHyphen(String phoneNumber) {
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanNoneMobile(phoneNumber, true));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "021234567", "0212345678", "0311234567", "03112345678",
                "07041234567", "05012345678", "0601234567", "0801234567"
        })
        @DisplayName("유효한 지역전화번호 (하이픈 없음)")
        void validLandlineWithoutHyphen(String phoneNumber) {
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanNoneMobile(phoneNumber, false));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "1544-1234", "1566-5678", "1577-9999", "1588-0000",
                "1599-1111", "1600-2222", "1644-3333", "1661-4444",
                "1666-5555", "1670-6666", "1688-7777"
        })
        @DisplayName("유효한 전국대표번호 (하이픈 포함)")
        void validNationalWithHyphen(String phoneNumber) {
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanNoneMobile(phoneNumber, true));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "15441234", "15665678", "15779999", "15880000",
                "15991111", "16002222", "16443333", "16614444",
                "16665555", "16706666", "16887777"
        })
        @DisplayName("유효한 전국대표번호 (하이픈 없음)")
        void validNationalWithoutHyphen(String phoneNumber) {
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanNoneMobile(phoneNumber, false));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "010-1234-5678", "011-1234-5678", "01012345678",  // 휴대전화 (제외되어야 함)
                "1500-1234", "1700-1234",                         // 잘못된 전국대표번호
                "invalid", "", "123-456-7890"
        })
        @DisplayName("유효하지 않은 비휴대전화번호")
        void invalidNonMobile(String phoneNumber) {
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanNoneMobile(phoneNumber, true));
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanNoneMobile(phoneNumber, false));
        }
    }

    @Nested
    @DisplayName("전화번호 타입 판별 테스트")
    class PhoneNumberTypeTest {

        @ParameterizedTest
        @ValueSource(strings = {
                "010-1234-5678", "01012345678", "011-1234-5678", "01112345678",
                "016-123-4567", "01612345678", "017-1234-5678", "0171234567",
                "018-123-4567", "01812345678", "019-1234-5678", "01912345678"
        })
        @DisplayName("휴대전화번호 타입 판별")
        void mobilePhoneTypeDetection(String phoneNumber) {
            assertEquals(KoreanPhoneNumberUtil.PhoneNumberType.MOBILE,
                    KoreanPhoneNumberUtil.getPhoneNumberType(phoneNumber));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "02-123-4567", "021234567", "02-1234-5678", "0212345678",
                "031-123-4567", "0311234567", "032-1234-5678", "03212345678",
                "070-4123-4567", "07041234567", "050-1234-5678", "05012345678",
                "060-123-4567", "0601234567", "080-123-4567", "0801234567"
        })
        @DisplayName("지역전화번호 타입 판별")
        void landlinePhoneTypeDetection(String phoneNumber) {
            assertEquals(KoreanPhoneNumberUtil.PhoneNumberType.LANDLINE,
                    KoreanPhoneNumberUtil.getPhoneNumberType(phoneNumber));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "1544-1234", "15441234", "1566-5678", "15665678",
                "1577-9999", "15779999", "1588-0000", "15880000",
                "1599-1111", "15991111", "1600-2222", "16002222",
                "1644-3333", "16443333", "1661-4444", "16614444",
                "1666-5555", "16665555", "1670-6666", "16706666",
                "1688-7777", "16887777"
        })
        @DisplayName("전국대표번호 타입 판별")
        void nationalPhoneTypeDetection(String phoneNumber) {
            assertEquals(KoreanPhoneNumberUtil.PhoneNumberType.NATIONAL,
                    KoreanPhoneNumberUtil.getPhoneNumberType(phoneNumber));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "123-456-7890", "012-1234-5678", "1500-1234", "1700-1234",
                "invalid", "",
        })
        @DisplayName("유효하지 않은 번호 타입 판별")
        void invalidPhoneTypeDetection(String phoneNumber) {
            assertEquals(KoreanPhoneNumberUtil.PhoneNumberType.INVALID,
                    KoreanPhoneNumberUtil.getPhoneNumberType(phoneNumber));
        }

        @Test
        @DisplayName("null 타입 판별")
        void nullPhoneTypeDetection() {
            assertEquals(KoreanPhoneNumberUtil.PhoneNumberType.INVALID,
                    KoreanPhoneNumberUtil.getPhoneNumberType(null));
        }
    }

    @Nested
    @DisplayName("전화번호 포맷팅 테스트")
    class PhoneNumberFormattingTest {

        @Test
        @DisplayName("휴대전화번호 포맷팅")
        void mobilePhoneFormatting() {
            assertEquals("010-1234-5678",
                    KoreanPhoneNumberUtil.toHyphenFormatPhoneNumber("01012345678"));
            assertEquals("010-123-4567",
                    KoreanPhoneNumberUtil.toHyphenFormatPhoneNumber("0101234567"));
            assertEquals("011-1234-5678",
                    KoreanPhoneNumberUtil.toHyphenFormatPhoneNumber("01112345678"));
        }

        @Test
        @DisplayName("서울 지역번호 포맷팅")
        void seoulLandlineFormatting() {
            assertEquals("02-123-4567",
                    KoreanPhoneNumberUtil.toHyphenFormatPhoneNumber("021234567"));
            assertEquals("02-1234-5678",
                    KoreanPhoneNumberUtil.toHyphenFormatPhoneNumber("0212345678"));
        }

        @Test
        @DisplayName("지역전화번호 포맷팅")
        void regionalLandlineFormatting() {
            assertEquals("031-123-4567",
                    KoreanPhoneNumberUtil.toHyphenFormatPhoneNumber("0311234567"));
            assertEquals("031-1234-5678",
                    KoreanPhoneNumberUtil.toHyphenFormatPhoneNumber("03112345678"));
            assertEquals("070-4123-4567",
                    KoreanPhoneNumberUtil.toHyphenFormatPhoneNumber("07041234567"));
            assertEquals("050-1234-5678",
                    KoreanPhoneNumberUtil.toHyphenFormatPhoneNumber("05012345678"));
        }

        @Test
        @DisplayName("전국대표번호 포맷팅")
        void nationalPhoneFormatting() {
            assertEquals("1588-1234",
                    KoreanPhoneNumberUtil.toHyphenFormatPhoneNumber("15881234"));
            assertEquals("1566-5678",
                    KoreanPhoneNumberUtil.toHyphenFormatPhoneNumber("15665678"));
            assertEquals("1577-9999",
                    KoreanPhoneNumberUtil.toHyphenFormatPhoneNumber("15779999"));
        }

        @Test
        @DisplayName("이미 포맷팅된 번호는 그대로 반환")
        void alreadyFormattedPhoneNumber() {
            assertEquals("010-1234-5678",
                    KoreanPhoneNumberUtil.toHyphenFormatPhoneNumber("010-1234-5678"));
            assertEquals("02-123-4567",
                    KoreanPhoneNumberUtil.toHyphenFormatPhoneNumber("02-123-4567"));
            assertEquals("1588-1234",
                    KoreanPhoneNumberUtil.toHyphenFormatPhoneNumber("1588-1234"));
        }

        @Test
        @DisplayName("유효하지 않은 번호는 원본 반환")
        void invalidPhoneNumberFormatting() {
            assertEquals("123456789",
                    KoreanPhoneNumberUtil.toHyphenFormatPhoneNumber("123456789"));
            assertEquals("invalid",
                    KoreanPhoneNumberUtil.toHyphenFormatPhoneNumber("invalid"));
            assertEquals("15001234",
                    KoreanPhoneNumberUtil.toHyphenFormatPhoneNumber("15001234"));
        }

        @Test
        @DisplayName("null 처리")
        void nullPhoneNumberFormatting() {
            assertNull(KoreanPhoneNumberUtil.toHyphenFormatPhoneNumber(null));
        }
    }

    @Nested
    @DisplayName("경계값 및 엣지 케이스 테스트")
    class EdgeCaseTest {

        @Test
        @DisplayName("빈 문자열 처리")
        void emptyStringHandling() {
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanPhoneNumber("", true));
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanPhoneNumber("", false));
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanMobile("", true));
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanMobile("", false));
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanNoneMobile("", true));
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanNoneMobile("", false));
            assertEquals(KoreanPhoneNumberUtil.PhoneNumberType.INVALID,
                    KoreanPhoneNumberUtil.getPhoneNumberType(""));
        }

        @Test
        @DisplayName("공백 문자열 처리")
        void whitespaceStringHandling() {
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanPhoneNumber("   ", true));
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanPhoneNumber("   ", false));
            assertEquals(KoreanPhoneNumberUtil.PhoneNumberType.INVALID,
                    KoreanPhoneNumberUtil.getPhoneNumberType("   "));
        }

        @Test
        @DisplayName("최소 자릿수 테스트")
        void minimumDigitsTest() {
            // 휴대전화 최소 자릿수 (10자리)
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanMobile("0101234567", false));
            assertEquals(KoreanPhoneNumberUtil.PhoneNumberType.MOBILE,
                    KoreanPhoneNumberUtil.getPhoneNumberType("0101234567"));

            // 서울 지역번호 최소 자릿수 (9자리)
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanNoneMobile("021234567", false));
            assertEquals(KoreanPhoneNumberUtil.PhoneNumberType.LANDLINE,
                    KoreanPhoneNumberUtil.getPhoneNumberType("021234567"));

            // 전국대표번호 자릿수 (8자리)
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanNoneMobile("15881234", false));
            assertEquals(KoreanPhoneNumberUtil.PhoneNumberType.NATIONAL,
                    KoreanPhoneNumberUtil.getPhoneNumberType("15881234"));
        }

        @Test
        @DisplayName("최대 자릿수 테스트")
        void maximumDigitsTest() {
            // 휴대전화 최대 자릿수 (11자리)
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanMobile("01012345678", false));
            assertEquals(KoreanPhoneNumberUtil.PhoneNumberType.MOBILE,
                    KoreanPhoneNumberUtil.getPhoneNumberType("01012345678"));

            // 지역전화 최대 자릿수 (11자리)
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanNoneMobile("07012345678", false));
            assertEquals(KoreanPhoneNumberUtil.PhoneNumberType.LANDLINE,
                    KoreanPhoneNumberUtil.getPhoneNumberType("07012345678"));
        }

        @Test
        @DisplayName("특수 번호 타입 테스트")
        void specialNumberTypesTest() {
            // 070 인터넷전화
            assertEquals(KoreanPhoneNumberUtil.PhoneNumberType.LANDLINE,
                    KoreanPhoneNumberUtil.getPhoneNumberType("07041234567"));

            // 050 개인번호
            assertEquals(KoreanPhoneNumberUtil.PhoneNumberType.LANDLINE,
                    KoreanPhoneNumberUtil.getPhoneNumberType("05012345678"));

            // 060 정보서비스
            assertEquals(KoreanPhoneNumberUtil.PhoneNumberType.LANDLINE,
                    KoreanPhoneNumberUtil.getPhoneNumberType("0601234567"));

            // 080 착신과금
            assertEquals(KoreanPhoneNumberUtil.PhoneNumberType.LANDLINE,
                    KoreanPhoneNumberUtil.getPhoneNumberType("0801234567"));
        }

        @Test
        @DisplayName("휴대전화와 비휴대전화 구분 테스트")
        void mobileVsNonMobileDistinction() {
            // 휴대전화는 isValidKoreanMobile에서만 true
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanMobile("01012345678", false));
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanNoneMobile("01012345678", false));

            // 지역전화는 isValidKoreanNoneMobile에서만 true
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanMobile("021234567", false));
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanNoneMobile("021234567", false));

            // 전국대표번호는 isValidKoreanNoneMobile에서만 true
            assertFalse(KoreanPhoneNumberUtil.isValidKoreanMobile("15881234", false));
            assertTrue(KoreanPhoneNumberUtil.isValidKoreanNoneMobile("15881234", false));
        }
    }
}