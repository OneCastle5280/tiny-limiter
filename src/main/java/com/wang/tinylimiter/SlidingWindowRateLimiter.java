package com.wang.tinylimiter;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 滑动窗口限速器
 * 两个关键：1. 如果计算当前时间落在哪个 slot 上  2. 如何清理过期的窗口和生成新的窗口
 *
 * @author wangjiabao
 */
@Data
public class SlidingWindowRateLimiter implements TinyLimiter{
    /**
     * 滑动窗口的小窗口数
     */
    private Integer slots;
    /**
     * 总限流数
     */
    private Integer limit;
    /**
     * 滑动窗口的长度（单位：ms）
     */
    private Integer windowSize;
    /**
     * 每一个小窗的限流大小
     */
    private Integer slotLimit;
    /**
     * 每一个小窗的长度（单位：ms）
     */
    private Integer duration;
    /**
     * 窗口开启时间
     */
    private AtomicLong startTime;
    /**
     * 上次清理过期窗口的时间
     */
    private AtomicLong lastClearTime;
    /**
     * 用来记录每一个小窗的请求数
     */
    private ConcurrentHashMap<Integer, AtomicInteger> countMap;

    public SlidingWindowRateLimiter(Integer slots, Integer limit, Integer windowSize) {
        if (windowSize % slots != 0 || limit % slots != 0) {
            throw new IllegalArgumentException("windowSize % slots != 0 || limit % slots != 0");
        }
        this.slots = slots;
        this.limit = limit;
        this.windowSize = windowSize;

        this.duration = this.windowSize / slots;
        this.slotLimit = limit / slots;
        this.startTime = new AtomicLong(System.currentTimeMillis());

        // init countMap
        this.initCountMap();

    }

    /**
     * 初始化计数器
     */
    private void initCountMap() {
        this.countMap = new ConcurrentHashMap<>(slots);
        for (int i = 0; i < slots; i++) {
            countMap.put(i, new AtomicInteger(0));
        }
    }

    @Override
    public boolean allow() {
        long currentTime = System.currentTimeMillis();
        // 计算当前所在的 slot
        Integer slot = this.getSlot(currentTime);
        // 先清理过期 slot
        this.cleanExpiredSlot(currentTime, slot);
        // 使用 computeIfAbsent 初始化新窗口
        AtomicInteger currentCount = countMap.computeIfAbsent(slot, k -> new AtomicInteger(0));
        // 判断是否超过限流
        if (currentCount.get() >= this.slotLimit) {
            return false;
        } else {
            // 并发场景下，这里可能会有并发问题，需要引用原子类来保证并发安全
            currentCount.incrementAndGet();
            this.countMap.put(slot, currentCount);
            return true;
        }
    }

    /**
     * 获取当前时间落在哪个 slot 上
     *
     * @param currentTime
     * @return
     */
    public int getSlot(long currentTime) {
        long countSlot = (currentTime - startTime.get()) / duration;
        return (int) (countSlot % slots);
    }

    private void cleanExpiredSlot(long currentTime, int currentSlotIndex) {
        // todo
    }
}
