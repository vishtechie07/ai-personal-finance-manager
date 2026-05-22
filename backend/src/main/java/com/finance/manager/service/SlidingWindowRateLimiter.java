package com.finance.manager.service;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory sliding-window counter (per JVM). Suitable for single-instance or best-effort limits.
 */
@Component
public class SlidingWindowRateLimiter {

    private final Map<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    /**
     * @return true if the request is allowed and recorded; false if the window is full.
     */
    public boolean tryConsume(String key, int maxRequests, long windowMs) {
        if (maxRequests <= 0) {
            return false;
        }
        long now = System.currentTimeMillis();
        Deque<Long> deque = buckets.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (deque) {
            while (!deque.isEmpty() && now - deque.peekFirst() > windowMs) {
                deque.pollFirst();
            }
            if (deque.size() >= maxRequests) {
                return false;
            }
            deque.addLast(now);
            return true;
        }
    }
}
