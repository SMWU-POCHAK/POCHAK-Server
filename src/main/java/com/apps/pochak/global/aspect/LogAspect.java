package com.apps.pochak.global.aspect;

import com.apps.pochak.global.api_payload.ApiResponse;
import com.apps.pochak.global.api_payload.exception.GeneralException;
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

import static com.apps.pochak.global.util.RequestInfo.createRequestFullPath;


@Aspect
@Component
@Slf4j
public class LogAspect {

    @Around("execution(* com.apps.pochak..*Controller.*(..)) && !execution(* com.apps.pochak..HomeController.*(..))")
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
            ApiResponse<?> entity = (ApiResponse<?>) response;
            log.info("Response status code = {}", entity.getCode());
        } catch (RuntimeException e) {
            log.error("RuntimeException occurred in method = {}, Error message = {}",
                    joinPoint.getSignature().getName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception occurred in method = {}, Error message = {}",
                    joinPoint.getSignature().getName(), e.getMessage());
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startAt;
            log.info("Execution time = {} ms", executionTime);
        }

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