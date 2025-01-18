package com.wang.tinylimiter;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class SlidingWindowRateLimiterTest {

    private SlidingWindowRateLimiter rateLimiter;

    @Before
    public void setUp() {
        // 设置一个具有 10 个槽和 100 个总限制的速率限制器，窗口大小为 1000 毫秒
        rateLimiter = new SlidingWindowRateLimiter(10, 100, 1000);
    }

    @Test
    public void allow_RequestWithinLimit_ShouldReturnTrue() {
        // 测试在限制内的请求
        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimiter.allow());
        }
    }

    @Test
    public void allow_RequestExceedsLimit_ShouldReturnFalse() {
        // 测试超过限制的请求
        for (int i = 0; i < 100; i++) {
            if (i < 10) {
                assertTrue(rateLimiter.allow());
            } else {
                assertFalse(rateLimiter.allow());
            }
        }
    }

    @Test
    public void allow_RequestAfterExpiration_ShouldReturnTrue() throws InterruptedException {
        // 测试在过期后请求
        for (int i = 0; i < 100; i++) {
            if (i < 10) {
                assertTrue(rateLimiter.allow());
            } else {
                assertFalse(rateLimiter.allow());
            }
        }
        // 等待窗口过期
        Thread.sleep(1000);
        // 再次测试请求
        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimiter.allow());
        }
    }

    @Test
    public void getSlot_ShouldReturnCorrectSlot() {
        long currentTime = System.currentTimeMillis();
        int slot = rateLimiter.getSlot(currentTime);
        // 假设窗口大小为 1000 毫秒，10 个槽，每个槽 100 毫秒
        assertTrue(slot >= 0 && slot < 10);
    }

    @Test
    public void cleanExpiredSlot_ShouldResetExpiredSlots() throws InterruptedException {
        // 测试过期槽的清理
        for (int i = 0; i < 100; i++) {
            if (i < 10) {
                assertTrue(rateLimiter.allow());
            } else {
                assertFalse(rateLimiter.allow());
            }
        }
        // 等待窗口过期
        Thread.sleep(1000);
        // 检查过期槽是否被重置
        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimiter.allow());
        }
    }

    @Test
    public void allow_MultiThreadedRequests_ShouldBeThreadSafe() throws InterruptedException {
        int numberOfThreads = 10;
        int requestsPerThread = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger allowedRequests = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    if (rateLimiter.allow()) {
                        allowedRequests.incrementAndGet();
                    }
                }
                latch.countDown();
            });
        }

        latch.await();
        executorService.shutdown();

        // 总请求数
        int totalRequests = numberOfThreads * requestsPerThread;
        // 预期允许的请求数
        int expectedAllowedRequests = Math.min(totalRequests, rateLimiter.getLimit());
        assertTrue("Allowed requests should not exceed the limit", allowedRequests.get() <= expectedAllowedRequests);
    }

    @Test
    public void allow_SpikeTraffic_ShouldBeThreadSafe() throws InterruptedException {
        // 设置一个具有 10 个槽和 100 个总限制的速率限制器，窗口大小为 1000 毫秒
        rateLimiter = new SlidingWindowRateLimiter(10, 100, 1000);

        int numberOfThreads = 20; // 多于槽的数量，模拟突刺流量
        int requestsPerThread = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger allowedRequests = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    if (rateLimiter.allow()) {
                        allowedRequests.incrementAndGet();
                    }
                }
                latch.countDown();
            });
        }

        latch.await();
        executorService.shutdown();

        // 总请求数
        int totalRequests = numberOfThreads * requestsPerThread;
        // 预期允许的请求数
        int expectedAllowedRequests = Math.min(totalRequests, rateLimiter.getLimit());
        assertTrue("Allowed requests should not exceed the limit", allowedRequests.get() <= expectedAllowedRequests);
        assertTrue("Allowed requests should be at least the number of slots", allowedRequests.get() >= rateLimiter.getSlots());
    }

    @Test
public void allow_FillEachSlotToLimit_ShouldReturnFalseForAdditionalRequests() throws InterruptedException {
        // 计算每个槽位的时间间隔
        long slotInterval = rateLimiter.getDuration();
        // 计算每个槽位的限流次数
        int requestsPerSlot = rateLimiter.getSlotLimit();

        // 在每个槽位中恰好达到限流次数
        for (int slot = 0; slot < rateLimiter.getSlots(); slot++) {
            for (int i = 0; i < requestsPerSlot; i++) {
                assertTrue(rateLimiter.allow());
            }
            assertFalse(rateLimiter.allow());
            // 等待进入下一个槽位
            System.out.println("Sleeping for " + slotInterval + " milliseconds...");
            Thread.sleep(slotInterval);
        }

        // 等待窗口过期
        Thread.sleep(rateLimiter.getWindowSize());
        // 再次测试请求，应该可以再次请求
        for (int slot = 0; slot < rateLimiter.getSlots(); slot++) {
            for (int i = 0; i < requestsPerSlot; i++) {
                assertTrue(rateLimiter.allow());
            }
            // 等待进入下一个槽位
            System.out.println("Sleeping for " + slotInterval + " milliseconds...");
            Thread.sleep(slotInterval);
        }
    }
}
