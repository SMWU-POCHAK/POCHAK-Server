package com.apps.pochak.login.util;

import com.apps.pochak.auth.Auth;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.api_payload.exception.handler.AuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus._INVALID_AUTHORITY;

@RequiredArgsConstructor
@Component
public class LoginArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.withContainingClass(Long.class)
                .hasParameterAnnotation(Auth.class);
    }

    @Override
    public Accessor resolveArgument(
            final MethodParameter parameter,
            final ModelAndViewContainer mavContainer,
            final NativeWebRequest webRequest,
            final WebDataBinderFactory binderFactory
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            final Long memberId = Long.valueOf(principal.toString());
            return Accessor.member(memberId);
        } catch (NumberFormatException e) {
            throw new AuthenticationException(_INVALID_AUTHORITY);
        }
    }
}
