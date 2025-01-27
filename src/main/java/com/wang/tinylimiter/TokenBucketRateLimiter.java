package com.wang.tinylimiter;

/**
 * 令牌桶限流器
 *
 * @author wangjiabao
 */
public class TokenBucketRateLimiter implements TinyLimiter{
    /**
     * 每一个 token 生成的时间间隔，单位 ms
     */
    private final Long interval;
    /**
     * 当前的 token 数量
     */
    private Long currentToken;
    /**
     * 当前令牌桶最大的 token 数量
     */
    private final Integer maxToken;
    /**
     * 下一个可以获取 Token 的时间戳，单位 ms
     */
    private Long nextAvailableGetTokenTime;
    /**
     * 每秒产生的 token 数量
     */
    private Integer tokenPerSecond;


    public TokenBucketRateLimiter(Integer tokenPerSecond) {
        this.tokenPerSecond = tokenPerSecond;
        this.interval = 1000L / tokenPerSecond;
        this.currentToken = 0L;
        this.maxToken = tokenPerSecond;
        this.nextAvailableGetTokenTime = 0L;
    }

    /**
     * 尝试获取 token，会理解返回
     *
     * @return
     */
    @Override
    public boolean allow() {
        synchronized (this) {
            long currentTimeMillis = System.currentTimeMillis();
            // refresh token
            this.refillToken(currentTimeMillis);
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
    private void refillToken(long currentTimeMillis) {
        if (currentTimeMillis > this.nextAvailableGetTokenTime) {
            // 当前时间超过下一个可获取 token 的时间，说明已经有 Token 过期了，需要补充
            long needAddToken = (currentTimeMillis - this.nextAvailableGetTokenTime) / this.interval;
            // 补充 Token，但不能超过 maxToken
            this.currentToken = Math.min(this.maxToken, this.currentToken + needAddToken);
            // 更新下一个可获取 Token 的时间为最后一次生成 Token 的时间点
            this.nextAvailableGetTokenTime += needAddToken * this.interval;
        }
    }
}
