package com.wang.tinylimiter;

/**
 * 漏桶限流器
 *
 * @author wangjiabao
 */
public class LeakyBucketRateLimiter implements TinyLimiter {

    /**
     * 每秒漏水速率
     */
    private Integer leakRatePerSec;
    /**
     * 上次的漏水时间，毫秒时间戳
     */
    private Long lastLeakTime;
    /**
     * 时间间隔，单位：ms
     */
    private Long interval;
    /**
     * 漏桶最大容量
     */
    private Integer capacity;
    /**
     * 当前漏桶内的水量
     */
    private Integer currentWater;

    public LeakyBucketRateLimiter(Integer leakRatePerSec, Integer capacity) {
        this.leakRatePerSec = leakRatePerSec;
        this.capacity = capacity;
        this.interval = 1000L / leakRatePerSec;
        this.lastLeakTime = System.currentTimeMillis();
        this.currentWater = 0;
    }

    @Override
    public boolean allow() {
        synchronized (this) {
            this.leakWater();
            if (this.currentWater < this.capacity) {
                this.currentWater++;
                return true;
            }
            return false;
        }
    }

    private void leakWater() {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis < this.lastLeakTime) {
            return;
        }
        // 距离上一次漏水，需要漏的水量
        long needLeakWater = (currentTimeMillis - this.lastLeakTime) / this.interval;
        this.currentWater = Math.max(0, this.currentWater - (int) needLeakWater);
        this.lastLeakTime = currentTimeMillis;
    }
}
