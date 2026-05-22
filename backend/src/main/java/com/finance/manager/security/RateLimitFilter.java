package com.finance.manager.security;

import com.finance.manager.config.AuthProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory rate limit for auth and AI endpoints (per client IP).
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final AuthProperties authProperties;
    private final Map<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return !(path.startsWith("/auth/") || path.startsWith("/ai/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = clientKey(request) + ":" + request.getServletPath();
        if (!allow(key)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean allow(String key) {
        long now = System.currentTimeMillis();
        long windowMs = authProperties.getRateLimitWindowSeconds() * 1000L;
        int max = authProperties.getRateLimitPerWindow();

        Deque<Long> deque = buckets.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (deque) {
            while (!deque.isEmpty() && now - deque.peekFirst() > windowMs) {
                deque.pollFirst();
            }
            if (deque.size() >= max) {
                return false;
            }
            deque.addLast(now);
            return true;
        }
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }
}
