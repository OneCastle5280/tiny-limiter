package com.wang.tinylimiter;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class LeakyBucketRateLimiterTest {

    // 1. 基本功能测试
    @Test
    public void testAllowWhenBucketNotFull() {
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(2, 5);
        assertTrue(limiter.allow()); // 允许
        assertTrue(limiter.allow()); // 允许
        assertTrue(limiter.allow()); // 允许
        assertTrue(limiter.allow()); // 允许
        assertTrue(limiter.allow()); // 允许
        assertFalse(limiter.allow()); // 桶已满，拒绝
    }

    // 3. 并发测试
    @Test
    public void testConcurrentAccess() throws InterruptedException {
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(10, 10);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        AtomicInteger allowedCount = new AtomicInteger(0);

        // 提交 100 个任务
        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> {
                if (limiter.allow()) {
                    allowedCount.incrementAndGet();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);

        // 由于漏桶容量为 10，最多允许 10 个请求
        System.out.println("允许的请求数量：" + allowedCount.get());
        assertTrue(allowedCount.get() <= 10);
    }

    // 4. 时间敏感性测试
    @Test
    public void testTimeSensitivity() throws InterruptedException {
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(2, 2);

        // 第一次请求
        assertTrue(limiter.allow()); // 允许，当前水量 = 1
        Thread.sleep(500); // 等待 500ms，漏出 1 单位水，当前水量 = 0

        // 第二次请求
        assertTrue(limiter.allow()); // 允许，当前水量 = 1
        Thread.sleep(500); // 等待 500ms，漏出 1 单位水，当前水量 = 0

        // 第三次请求
        assertTrue(limiter.allow()); // 允许，当前水量 = 1
        Thread.sleep(500); // 等待 500ms，漏出 1 单位水，当前水量 = 0

        // 第四次请求
        assertTrue(limiter.allow()); // 允许，当前水量 = 1
        Thread.sleep(500); // 等待 500ms，漏出 1 单位水，当前水量 = 0

        // 第五次请求
        assertTrue(limiter.allow()); // 允许，当前水量 = 1
        Thread.sleep(500); // 等待 500ms，漏出 1 单位水，当前水量 = 0

        // 第六次请求
        assertTrue(limiter.allow()); // 允许，当前水量 = 1
        // 第七次请求
        assertTrue(limiter.allow()); // 允许，当前水量 = 2
        // 第八次请求
        assertFalse(limiter.allow()); // 桶已满，拒绝
        Thread.sleep(500); // 等待 500ms，漏出 1 单位水，当前水量 = 0
        assertTrue(limiter.allow()); // 允许，当前水量 = 1
    }

    // 5. 综合测试
    @Test
    public void testComprehensive() throws InterruptedException {
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(2, 5);

        // 初始状态
        assertTrue(limiter.allow()); // 允许
        assertTrue(limiter.allow()); // 允许
        assertTrue(limiter.allow()); // 允许
        assertTrue(limiter.allow()); // 允许
        assertTrue(limiter.allow()); // 允许
        assertFalse(limiter.allow()); // 桶已满，拒绝

        // 等待一段时间后漏水
        Thread.sleep(1000); // 等待 1 秒
        assertTrue(limiter.allow()); // 允许（漏水后）
    }
}