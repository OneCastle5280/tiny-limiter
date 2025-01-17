package com.wang.tinylimiter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 滑动窗口限速器
 * 两个关键：1. 如果计算当前时间落在哪个 slot 上  2. 如果清理过期的窗口和生成新的窗口
 *
 * @author wangjiabao
 */
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
     * 窗口大小（单位：ms）
     */
    private Integer windowSize;
    /**
     * 时间间隔
     */
    private Integer duration;
    /**
     * 窗口开启时间
     */
    private Long startTime;
    /**
     * 用来记录每一个小窗的请求数
     */
    private ConcurrentHashMap<Integer, Integer> countMap;

    public SlidingWindowRateLimiter(Integer slots, Integer limit, Integer windowSize) {
        if (windowSize % slots != 0) {
            throw new IllegalArgumentException("The windowSize must be evenly divisible by the number of slots");
        }
        this.slots = slots;
        this.limit = limit;
        this.windowSize = windowSize;

        this.duration = windowSize / slots;
        this.startTime = System.currentTimeMillis();

        // init countMap
        this.initCountMap();
    }

    /**
     * 初始化计数器
     */
    private void initCountMap() {
        for (int i = 0; i < slots; i++) {
            countMap.put(i, 0);
        }
    }

    @Override
    public boolean allow() {
        // 对于每一个请求进来，首先判断在哪个槽
    }

    private Integer getSlot(long currentTime) {
        // 计算相比较于窗口开启时间
        long intervalTime = currentTime - startTime;
        // 需要走几个时间槽
        long countSlot = intervalTime / duration;
        return (int) (countSlot % slots);
    }
}
