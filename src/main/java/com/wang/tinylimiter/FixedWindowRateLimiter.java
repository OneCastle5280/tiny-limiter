package com.wang.tinylimiter;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 固定窗口限流器
 *
 * @author wangjiabao
 */
public class FixedWindowRateLimiter implements TinyLimiter{
    /**
     * 可重入锁
     */
    private final ReentrantLock lock;
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
    private Integer count;
    /**
     * 窗口起始时间
     */
    private Long startTime;

    /**
     * init
     *
     * @param windowSize
     * @param limit
     */
    public FixedWindowRateLimiter(Integer windowSize, Integer limit) {
        this.windowSize = windowSize;
        this.limit = limit;

        this.count = 0;
        this.lock = new ReentrantLock();
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public boolean allow() {
        // 可重入锁，防并发
        lock.lock();
        try {
            // 获取当前时间
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime >= windowSize) {
                // 已经进入到了下一个
                this.reset(currentTime);
            }
            count ++;
            return count <= limit;
        } finally {
            lock.unlock();
        }
    }

    private void reset(long currentTime) {
        this.count = 0;
        this.startTime = currentTime;
    }
}
