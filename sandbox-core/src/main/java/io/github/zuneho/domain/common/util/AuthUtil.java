package io.github.zuneho.domain.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Optional;

@UtilityClass
public class AuthUtil {
    private static final String AUTH_HEADER_NAME = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    //FOR APP API
    private static final String APP_API_TOKEN_PARAM = "token";
    private static final String APP_API_TOKEN_COOKIE_NAME = "imin";
    private static final String APP_API_UNAUTHORIZED_MEMBER_NAME = "UNKNOWN_USER";

    public static String getAppApiTokenFromRequest(HttpServletRequest request) {
        String paramToken = getTokenFromParam(request, APP_API_TOKEN_PARAM);
        if (paramToken != null) {
            return paramToken;
        }
        return getBarerTokenFromHeader(request);
        //return getTokenFromCookie(request, APP_API_TOKEN_COOKIE_NAME); //현재 쿠키는 논의된 바가 없음
    }

    public static String getTokenFromCookie(HttpServletRequest request, String tokenCookieName) {
        return getCookie(request, tokenCookieName)
                .map(Cookie::getValue)
                .orElse(null);
    }

    public static Optional<Cookie> getCookie(HttpServletRequest request, String cookieName) {
        if (request == null || request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findFirst();
    }

    public static String getTokenFromParam(HttpServletRequest request, String tokenParamName) {
        if (request == null) {
            return null;
        }
        return request.getParameter(tokenParamName);
    }

    public static String getBarerTokenFromHeader(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String authHeader = request.getHeader(AUTH_HEADER_NAME);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }

        return null;
    }
//
//    public static boolean isAuthenticatedRequest() {
//        return getCurrentAppApiMember() != null;
//    }
//
//    public static Long getCurrentAppApiMemberIdx() {
//        AppMemberDetailDto appMemberDetailDto = getCurrentAppApiMember();
//        if (appMemberDetailDto == null) {
//            return null;
//        }
//        return appMemberDetailDto.getIdx();
//    }
//
//    public static AppMemberDetailDto getCurrentAppApiMember() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null) {
//            return null;
//        }
//        Object principal = authentication.getPrincipal();
//        if (principal instanceof AppMemberDetailDto) {
//            return (AppMemberDetailDto) principal;
//        }
//        return null;
//    }
}
