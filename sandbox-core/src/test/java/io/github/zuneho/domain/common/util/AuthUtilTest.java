package io.github.zuneho.domain.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthUtilTest {

    @Test
    @DisplayName("헤더에서 Bearer 토큰을 정상적으로 추출한다.")
    void testGetBearerTokenFromHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer test-token");

        String token = AuthUtil.getBarerTokenFromHeader(request);

        assertEquals("test-token", token);
    }

    @Test
    @DisplayName("헤더가 없으면 null을 반환한다.")
    void testGetBearerTokenFromHeader_NullHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        String token = AuthUtil.getBarerTokenFromHeader(request);

        assertNull(token);
    }

    @Test
    @DisplayName("Bearer 접두사가 없으면 null을 반환한다.")
    void testGetBearerTokenFromHeader_NoBearerPrefix() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic test-token");

        String token = AuthUtil.getBarerTokenFromHeader(request);

        assertNull(token);
    }

    @Test
    @DisplayName("쿠키에서 토큰을 정상적으로 추출한다.")
    void testGetTokenFromCookie() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie cookie = new Cookie("auth-token", "cookie-token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        String token = AuthUtil.getTokenFromCookie(request, "auth-token");

        assertEquals("cookie-token", token);
    }

    @Test
    @DisplayName("해당하는 쿠키가 없으면 null을 반환한다.")
    void testGetTokenFromCookie_NoMatchingCookie() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie cookie = new Cookie("other-token", "some-token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        String token = AuthUtil.getTokenFromCookie(request, "auth-token");

        assertNull(token);
    }

    @Test
    @DisplayName("쿠키가 없는 경우 null을 반환한다.")
    void testGetTokenFromCookie_NoCookies() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        String token = AuthUtil.getTokenFromCookie(request, "auth-token");

        assertNull(token);
    }

    @Test
    @DisplayName("요청 파라미터에서 토큰을 정상적으로 추출한다.")
    void testGetTokenFromParam() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("token")).thenReturn("param-token");

        String token = AuthUtil.getTokenFromParam(request, "token");

        assertEquals("param-token", token);
    }

    @Test
    @DisplayName("요청 파라미터가 없으면 null을 반환한다.")
    void testGetTokenFromParam_NoParam() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("token")).thenReturn(null);

        String token = AuthUtil.getTokenFromParam(request, "token");

        assertNull(token);
    }

    @Test
    @DisplayName("쿠키 객체를 정상적으로 가져온다.")
    void testGetCookie() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie cookie = new Cookie("session", "session-value");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        Optional<Cookie> result = AuthUtil.getCookie(request, "session");

        assertTrue(result.isPresent());
        assertEquals("session-value", result.get().getValue());
    }

    @Test
    @DisplayName("해당하는 쿠키가 없으면 Optional.empty()를 반환한다.")
    void testGetCookie_NoMatchingCookie() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie cookie = new Cookie("other", "other-value");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        Optional<Cookie> result = AuthUtil.getCookie(request, "session");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("쿠키가 없는 경우 Optional.empty()를 반환한다.")
    void testGetCookie_NoCookies() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        Optional<Cookie> result = AuthUtil.getCookie(request, "session");

        assertFalse(result.isPresent());
    }
}