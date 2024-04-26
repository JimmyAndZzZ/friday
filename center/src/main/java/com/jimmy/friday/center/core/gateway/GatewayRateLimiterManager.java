package com.jimmy.friday.center.core.gateway;

import com.google.common.collect.Maps;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class GatewayRateLimiterManager {

    private final ConcurrentMap<String, RateLimiter> rateLimiterMap = Maps.newConcurrentMap();

    public RateLimiter getRateLimiter(String name) {
        RateLimiterConfig config = RateLimiterConfig.custom().limitForPeriod(50) // 每个周期内的限流次数
                .limitRefreshPeriod(Duration.ofSeconds(1)) // 限流周期
                .timeoutDuration(Duration.ofMillis(500)) // 超时时间
                .build();

        RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(config);

        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(name, config);

        RateLimiter ifAbsent = rateLimiterMap.putIfAbsent(name, rateLimiter);
        if (ifAbsent != null) {
            rateLimiter = null;
            return ifAbsent;
        }

        return rateLimiter;
    }

}
