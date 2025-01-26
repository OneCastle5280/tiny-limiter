package com.wang.tinylimiter;


import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TokenBucketRateLimiterTest {

    private TokenBucketRateLimiter rateLimiter;

    @Before
    public void setUp() {
        rateLimiter = new TokenBucketRateLimiter(10);
    }

    @Test
    public void allow_WithAvailableTokens_ShouldReturnTrueAndDecrementTokens() {
        // 令牌桶初始有10个令牌
        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimiter.allow());
        }
    }

    @Test
    public void allow_WithNoAvailableTokens_ShouldReturnFalse() {
        // 消耗所有令牌
        for (int i = 0; i < 10; i++) {
            rateLimiter.allow();
        }
        // 再次尝试获取令牌
        assertFalse(rateLimiter.allow());
    }

    @Test
    public void allow_AfterTokenRefresh_ShouldReturnTrue() throws InterruptedException {
        // 消耗所有令牌
        for (int i = 0; i < 10; i++) {
            rateLimiter.allow();
        }
        // 等待一段时间以允许令牌刷新
        Thread.sleep(100);
        assertTrue(rateLimiter.allow());
    }

    @Test
    public void allow_TokenCountShouldNotExceedMaxToken() throws InterruptedException {
        // 消耗所有令牌
        for (int i = 0; i < 10; i++) {
            rateLimiter.allow();
        }
        // 等待一段时间以允许令牌刷新
        Thread.sleep(1000);
        // 再次消耗所有令牌
        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimiter.allow());
        }
        // 再次尝试获取令牌
        assertFalse(rateLimiter.allow());
    }
}
