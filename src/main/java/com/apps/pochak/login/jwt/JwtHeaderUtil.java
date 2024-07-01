package com.apps.pochak.login.jwt;

import com.apps.pochak.global.api_payload.exception.GeneralException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static com.apps.pochak.global.Constant.*;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus._UNAUTHORIZED;

public class JwtHeaderUtil {

    public static String getAccessToken() {
        return getToken(HEADER_AUTHORIZATION);
    }

    public static String getRefreshToken() {
        return getToken(HEADER_REFRESH_TOKEN);
    }

    private static String getToken(String tokenHeader) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String headerValue = request.getHeader(tokenHeader);
        if (headerValue == null) throw new GeneralException(_UNAUTHORIZED);
        if (StringUtils.hasText(headerValue) && headerValue.startsWith(TOKEN_PREFIX)) {
            return headerValue.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
