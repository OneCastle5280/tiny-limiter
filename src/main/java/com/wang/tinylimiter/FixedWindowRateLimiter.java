package com.wang.tinylimiter;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 固定窗口限流器
 *
 * @author wangjiabao
 */
public class FixedWindowRateLimiter implements TinyLimiter{
    /**
     * 固定窗口大小（单位：ms）
     */
    private final Integer windowSize;
    /**
     * 固定窗口限流数
     */
    private final Integer limit;
    /**
     * 计数器
     */
    private final AtomicLong count;
    /**
     * 窗口起始时间
     */
    private final AtomicLong startTime;

    /**
     * init
     *
     * @param windowSize
     * @param limit
     */
    public FixedWindowRateLimiter(Integer windowSize, Integer limit) {
        this.windowSize = windowSize;
        this.limit = limit;

        this.count = new AtomicLong(0);
        this.startTime = new AtomicLong(System.currentTimeMillis());
    }

    @Override
    public boolean allow() {
        // 获取当前时间
        long currentTime = System.currentTimeMillis();
        if (currentTime - startTime.get() >= windowSize) {
            // 已经进入到了下一个窗口
            this.reset(currentTime);
        }
        return this.count.incrementAndGet() <= limit;
    }

    private void reset(long currentTime) {
        // 采用 CAS 乐观锁的形式来更新，提升并发度
        this.count.compareAndSet(this.count.get(), 0);
        this.startTime.compareAndSet(this.startTime.get(), currentTime);
    }
}
