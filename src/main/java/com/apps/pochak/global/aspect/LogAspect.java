package com.apps.pochak.global.aspect;

import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.global.util.RequestInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;

import java.util.Objects;

import static com.apps.pochak.global.util.RequestInfo.createRequestFullPath;


@Aspect
@Component
@Slf4j
public class LogAspect {

    @Around("execution(* com.apps.pochak..*Controller.*(..))")
    public Object apiLogging(ProceedingJoinPoint joinPoint) throws Throwable {
        long startAt = System.currentTimeMillis();

        String IpAddress;
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String requestPath = createRequestFullPath(request);
            IpAddress = request.getRemoteAddr();
            log.info("{}", requestPath);
            log.info("Request from = {}", IpAddress);
        }

        String requester = getRequester();
        log.info("Requester authentication = {}", requester);

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
        } catch (GeneralException e) {
            log.error("Exception occurred in method = {}, Error message = {}",
                    joinPoint.getSignature().getName(), e.getMessage());
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startAt;
            log.info("Execution time : {} ms", executionTime);
        }
        log.info("Response Return type = {}, Return value = {}", response.getClass().getSimpleName(), response);

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