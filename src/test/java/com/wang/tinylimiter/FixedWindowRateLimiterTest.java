package com.wang.tinylimiter;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class FixedWindowRateLimiterTest {

    private FixedWindowRateLimiter rateLimiter;

    @Before
    public void setUp() {
        rateLimiter = new FixedWindowRateLimiter(1000, 5); // 1秒窗口，限制为5
    }

    @Test
    public void allow_WithinLimit_ReturnsTrue() {
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.allow());
        }
    }

    @Test
    public void allow_ExceedsLimit_ReturnsFalse() {
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.allow());
        }
        assertFalse(rateLimiter.allow());
    }

    @Test
    public void allow_AfterWindowReset_ReturnsTrue() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.allow());
        }
        Thread.sleep(1000); // 等待窗口重置
        assertTrue(rateLimiter.allow());
    }

    @Test
    public void allow_MultiThreadedAccess() throws InterruptedException {
        final int THREADS = 10;
        final int LIMIT = 5;
        final int TOTAL_REQUESTS = 100;
        final CountDownLatch latch = new CountDownLatch(THREADS);
        final AtomicInteger allowedCount = new AtomicInteger(0);

        for (int i = 0; i < THREADS; i++) {
            new Thread(() -> {
                for (int j = 0; j < TOTAL_REQUESTS / THREADS; j++) {
                    if (rateLimiter.allow()) {
                        allowedCount.incrementAndGet();
                    }
                }
                latch.countDown();
            }).start();
        }

        latch.await();
        assertTrue(allowedCount.get() <= LIMIT);
    }
}