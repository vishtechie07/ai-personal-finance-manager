package com.finance.manager.security;

import com.finance.manager.config.AuthProperties;
import com.finance.manager.util.ClientIpResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory rate limit for auth, AI, import, and account mutation endpoints (per client IP).
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
        return limitsFor(request) == null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        RateLimitSpec spec = limitsFor(request);
        if (spec == null) {
            filterChain.doFilter(request, response);
            return;
        }
        String key = ClientIpResolver.resolve(request) + ":" + request.getMethod() + ":" + request.getServletPath();
        if (!allow(key, spec.max(), spec.windowMs())) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private RateLimitSpec limitsFor(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getServletPath();

        if (HttpMethod.POST.matches(method) && path.startsWith("/auth/")) {
            if ("/auth/register".equals(path)) {
                return new RateLimitSpec(
                        authProperties.getRegisterAttemptsPerWindow(),
                        authProperties.getRegisterAttemptWindowSeconds() * 1000L);
            }
            return new RateLimitSpec(
                    authProperties.getRateLimitPerWindow(),
                    authProperties.getRateLimitWindowSeconds() * 1000L);
        }
        if (HttpMethod.POST.matches(method) && path.startsWith("/ai/")) {
            return new RateLimitSpec(
                    authProperties.getRateLimitPerWindow(),
                    authProperties.getRateLimitWindowSeconds() * 1000L);
        }
        if (HttpMethod.POST.matches(method) && "/transactions/import".equals(path)) {
            return new RateLimitSpec(5, 3_600_000L);
        }
        if (HttpMethod.POST.matches(method) && "/auth/me/change-password".equals(path)) {
            return new RateLimitSpec(5, 3_600_000L);
        }
        if (HttpMethod.DELETE.matches(method) && "/auth/me".equals(path)) {
            return new RateLimitSpec(3, 3_600_000L);
        }
        if (HttpMethod.POST.matches(method) && path.startsWith("/category-rules")) {
            return new RateLimitSpec(20, authProperties.getRateLimitWindowSeconds() * 1000L);
        }
        return null;
    }

    private boolean allow(String key, int max, long windowMs) {
        long now = System.currentTimeMillis();
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

    private record RateLimitSpec(int max, long windowMs) {}
}
