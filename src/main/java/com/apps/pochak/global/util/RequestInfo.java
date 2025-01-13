package com.apps.pochak.global.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

public class RequestInfo {

    public static String createRequestFullPath(final HttpServletRequest httpServletRequest) {
        String fullPath = httpServletRequest.getMethod() + " " + httpServletRequest.getRequestURL();

        String queryString = httpServletRequest.getQueryString();
        if (queryString != null) {
            fullPath += "?" + queryString;
        }
        return fullPath;
    }

    public static String createRequestFullPath(final WebRequest webRequest) {
        HttpServletRequest request = ((ServletWebRequest) webRequest).getRequest();
        return createRequestFullPath(request);
    }
}
