package com.tasktracker.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Production-safe in-memory rate limiter.
 * - Sliding time window per key
 * - Thread-safe updates
 * - Auto-reset window
 * - Lightweight (no Redis required)
 */
@Component
public class RateLimiter {

    private static final long WINDOW_MILLIS = 15 * 60 * 1000L; // 15 minutes

    private static class Bucket {
        final AtomicInteger count = new AtomicInteger(0);
        volatile long windowStart;

        Bucket(long windowStart) {
            this.windowStart = windowStart;
        }
    }

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean allow(String key, int maxAttempts) {
        long now = Instant.now().toEpochMilli();

        Bucket bucket = buckets.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStart > WINDOW_MILLIS) {
                return new Bucket(now);
            }
            return existing;
        });

        int current = bucket.count.incrementAndGet();

        // reset window if expired (extra safety check)
        if (now - bucket.windowStart > WINDOW_MILLIS) {
            bucket.windowStart = now;
            bucket.count.set(1);
            return true;
        }

        return current <= maxAttempts;
    }
}