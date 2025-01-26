package com.wang.tinylimiter;

import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 令牌桶限流器
 *
 * @author wangjiabao
 */
public class TokenBucketRateLimiter implements TinyLimiter{
    /**
     * 每一个 token 生成的时间间隔，单位 ms
     */
    private Long interval;
    /**
     * 当前的 token 数量
     */
    private Long currentToken;
    /**
     * 当前令牌桶最大的 token 数量
     */
    private Long maxToken;
    /**
     * 下一个可以获取 Token 的时间戳，单位 ms
     */
    private Long nextAvailableGetTokenTime;
    /**
     * 每秒产生的 token 数量
     */
    private Integer tokenPerSecond;
    /**
     * 锁
     */
    private volatile Object mutex;

    /**
     * 双重判断
     *
     * @return
     */
    private Object mutex() {
        if (mutex == null) {
            synchronized (this) {
                if (mutex == null) {
                    mutex = new Object();
                }
            }
        }
        return mutex;
    }

    public TokenBucketRateLimiter(Integer tokenPerSecond) {
        this.tokenPerSecond = tokenPerSecond;
        this.interval = 1000L / tokenPerSecond;
        this.currentToken = 0L;
        this.maxToken = tokenPerSecond.longValue();
        this.nextAvailableGetTokenTime = 0L;
    }

    /**
     * 尝试获取 token，会理解返回
     *
     * @return
     */
    @Override
    public boolean allow() {
        synchronized (mutex()) {
            long currentTimeMillis = System.currentTimeMillis();
            // refresh token
            this.refreshToken(currentTimeMillis);
            if (this.currentToken > 0) {
                this.currentToken--;
                return true;
            }
            return false;
        }
    }

    /**
     * 刷新 token
     *
     * @param currentTimeMillis
     */
    private void refreshToken(long currentTimeMillis) {
        if (currentTimeMillis > this.nextAvailableGetTokenTime) {
            // 当前时间超过下一个可获取 token 的时间，说明已经有 Token 过期了，需要补充
            long needAddToken = (currentTimeMillis - this.nextAvailableGetTokenTime) / this.interval;
            this.currentToken = Math.min(this.maxToken, this.currentToken + needAddToken);
            this.nextAvailableGetTokenTime = currentTimeMillis;
        }
    }
}
