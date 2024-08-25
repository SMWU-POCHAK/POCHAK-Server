package com.apps.pochak.auth;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.api_payload.exception.handler.AuthenticationException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus._INVALID_AUTHORITY;

@Aspect
@Component
public class MemberOnlyChecker {

    @Before("@annotation(com.apps.pochak.auth.MemberOnly)")
    public void check(final JoinPoint joinPoint) {
        Arrays.stream(joinPoint.getArgs())
                .filter(Accessor.class::isInstance)
                .map(Accessor.class::cast)
                .filter(Accessor::isMember)
                .findFirst()
                .orElseThrow(() -> new AuthenticationException(_INVALID_AUTHORITY));
    }
}
