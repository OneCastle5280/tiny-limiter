package com.wang.tinylimiter;


import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Test
    public void testConcurrentAccess() throws InterruptedException {
        // 创建一个限流器：每秒最多处理 5 个请求
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(5);
        // 线程数
        int threadCount = 10;
        // 每个线程的请求次数
        int requestsPerThread = 10;
        // 总请求数
        int totalRequests = threadCount * requestsPerThread;
        // 用于统计通过的请求数
        AtomicInteger allowedRequests = new AtomicInteger(0);
        // 使用 CountDownLatch 控制线程同时开始
        CountDownLatch startLatch = new CountDownLatch(1);
        // 使用 CountDownLatch 等待所有线程完成
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        // 创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        // 提交任务
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // 等待所有线程就绪
                    startLatch.await();
                    // 每个线程执行多次请求
                    for (int j = 0; j < requestsPerThread; j++) {
                        if (rateLimiter.allow()) {
                            allowedRequests.incrementAndGet();
                        }
                        // 模拟请求间隔
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    // 线程完成任务
                    endLatch.countDown();
                }
            });
        }
        // 启动所有线程
        startLatch.countDown();
        // 等待所有线程完成
        endLatch.await();
        // 关闭线程池
        executorService.shutdown();
        // 验证通过的请求数是否符合预期
        System.out.println("Total requests: " + totalRequests);
        System.out.println("Allowed requests: " + allowedRequests.get());
        // 由于限流器每秒最多允许 5 个请求，因此在 2 秒内最多允许 10 个请求
        // 这里我们放宽条件，允许一定的误差
        assertTrue("Allowed requests should not exceed the limit", allowedRequests.get() <= 10);
    }
}
