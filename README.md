# tiny-limiter

`tiny-limiter` 实现了四种限流算法，主要用于实践学习到的理论知识~

## 目录
- [令牌桶限流器 (Token Bucket Rate Limiter)](#令牌桶限流器-token-bucket-rate-limiter)
- [漏桶限流器 (Leaky Bucket Rate Limiter)](#漏桶限流器-leaky-bucket-rate-limiter)
- [固定窗口计数器限流器 (Fixed Window Rate Limiter)](#固定窗口计数器限流器-fixed-window-rate-limiter)
- [滑动窗口日志限流器 (Sliding Window Rate Limiter)](#滑动窗口日志限流器-sliding-window-rate-limiter)


## 令牌桶限流器 (Token Bucket Rate Limiter)

令牌桶限流器是一种常用的限流算法，它以固定的速率向桶中添加令牌。当请求到达时，只有在桶中有令牌时才能被处理，并且会消耗一个令牌。

### 使用方法
```java
package com.wang.tinylimiter.example;

import com.wang.tinylimiter.TokenBucketRateLimiter;
import com.wang.tinylimiter.TinyLimiter;

public class TokenBucketExample {
    public static void main(String[] args) {
        TinyLimiter rateLimiter = new TokenBucketRateLimiter(10); // 每秒生成10个令牌
        for (int i = 0; i < 20; i++) {
            if (rateLimiter.allow()) {
                System.out.println("Request " + i + " is allowed.");
            } else {
                System.out.println("Request " + i + " is rejected.");
            }
        }
    }
}

```

## 漏桶限流器 (Leaky Bucket Rate Limiter)

漏桶限流器是一种简单而有效的限流算法，它以固定的速率处理请求，多余的请求会被放入桶中，直到桶满为止。如果桶满了，新的请求会被丢弃。

### 使用方法
```java
package com.wang.tinylimiter.example;

import com.wang.tinylimiter.LeakyBucketRateLimiter;
import com.wang.tinylimiter.TinyLimiter;

public class LeakyBucketExample {
    public static void main(String[] args) {
        TinyLimiter rateLimiter = new LeakyBucketRateLimiter(1, 10); // 每秒漏水速率1，桶的最大容量为10
        for (int i = 0; i < 20; i++) {
            if (rateLimiter.allow()) {
                System.out.println("Request " + i + " is allowed.");
            } else {
                System.out.println("Request " + i + " is rejected.");
            }
        }
    }
}

```

## 固定窗口计数器限流器 (Fixed Window Rate Limiter)

固定窗口计数器限流器将时间划分为固定大小的窗口，并在每个窗口内记录请求数量。如果请求数量超过阈值，则拒绝新的请求。

### 使用方法
```java
package com.wang.tinylimiter.example;

import com.wang.tinylimiter.FixedWindowRateLimiter;
import com.wang.tinylimiter.TinyLimiter;

public class FixedWindowCounterExample {
    public static void main(String[] args) {
        TinyLimiter rateLimiter = new FixedWindowRateLimiter(1000, 10); // 每秒允许10个请求， 1000 即 1s
        for (int i = 0; i < 20; i++) {
            if (rateLimiter.allow()) {
                System.out.println("Request " + i + " is allowed.");
            } else {
                System.out.println("Request " + i + " is rejected.");
            }
        }
    }
}

```


## 滑动窗口日志限流器 (Sliding Window Rate Limiter)

滑动窗口日志限流器通过记录每个请求的时间戳，并在当前时间窗口内统计请求数量来实现限流。如果请求数量超过阈值，则拒绝新的请求。

### 使用方法
TODO 滑动窗口待完善


## 贡献
欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个 Pull Request

## 许可证

本项目采用 [MIT 许可证](LICENSE)。
