package com.apps.pochak.login.jwt;

import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.global.api_payload.exception.handler.InvalidJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static com.apps.pochak.global.Constant.*;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.INVALID_OAUTH_TOKEN;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.NULL_TOKEN;

public class JwtHeaderUtil {

    public static String getAccessToken() {
        return getToken(HEADER_AUTHORIZATION);
    }

    public static String getRefreshToken() {
        return getToken(HEADER_REFRESH_TOKEN);
    }

    private static String getToken(final String tokenHeader) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String headerValue = request.getHeader(tokenHeader);
        if (headerValue == null || headerValue.isEmpty()) throw new GeneralException(NULL_TOKEN);
        if (StringUtils.hasText(headerValue) && headerValue.startsWith(TOKEN_PREFIX)) {
            return headerValue.substring(TOKEN_PREFIX.length());
        }
        throw new InvalidJwtException(INVALID_OAUTH_TOKEN);
    }
}
