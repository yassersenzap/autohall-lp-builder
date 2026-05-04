package com.autohall.lpbuilderapi.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class AdminApiKeyInterceptor implements HandlerInterceptor {

    private final String adminApiKey;

    public AdminApiKeyInterceptor(@Value("${app.security.admin-api-key:}") String adminApiKey) {
        this.adminApiKey = adminApiKey;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Temporary V1 guard. Replace with Spring Security before production.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (!StringUtils.hasText(adminApiKey)) {
            response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value(), "Admin API key is not configured");
            return false;
        }

        String providedKey = request.getHeader("X-Admin-Api-Key");
        if (!matches(providedKey, adminApiKey)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing or invalid admin API key");
            return false;
        }

        return true;
    }

    private boolean matches(String providedKey, String expectedKey) {
        if (!StringUtils.hasText(providedKey)) {
            return false;
        }

        byte[] provided = providedKey.getBytes(StandardCharsets.UTF_8);
        byte[] expected = expectedKey.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(provided, expected);
    }
}
