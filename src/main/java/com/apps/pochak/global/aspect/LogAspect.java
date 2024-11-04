package com.apps.pochak.global.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;


@Aspect
@Component
@Slf4j
public class LogAspect {

    @Around("execution(* com.apps.pochak..*Controller.*(..))")
    public Object apiLogging(ProceedingJoinPoint joinPoint) throws Throwable {
        long startAt = System.currentTimeMillis();

        String requester = getRequester();
        HttpServletRequest request =
                ((ServletRequestAttributes) Objects
                        .requireNonNull(RequestContextHolder.getRequestAttributes())
                ).getRequest();
        String IpAddress = request.getRemoteAddr();
        log.info("Request from = {}, IP Address = {}", requester, IpAddress);

        Signature signature = joinPoint.getSignature();
        log.info("Method called = {}", signature.toShortString());

        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            for (Object arg : args) {
                log.info("Parameter type = {}, value = {}", arg.getClass().getSimpleName(), arg);
            }
        } else log.info("Parameter = no parameter");

        Object response;
        try {
            response = joinPoint.proceed();
        } finally {
            long executionTime = System.currentTimeMillis() - startAt;
            log.info("Execution time : {} ms", executionTime);
        }
        log.info("Response");
        log.info("Return type = {}, Return value = {}", response.getClass().getSimpleName(), response);

        return response;
    }

    private String getRequester() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "Anonymous";
    }
}