package com.jimmy.friday.center.core.gateway;

import com.google.common.collect.Maps;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class GatewayCircuitBreakerManager {

    private final ConcurrentMap<String, CircuitBreaker> circuitBreakerMap = Maps.newConcurrentMap();

    public CircuitBreaker getCircuitBreaker(String name) {
        CircuitBreaker circuitBreaker = CircuitBreaker.of(name, CircuitBreakerConfig.ofDefaults());

        CircuitBreaker ifAbsent = circuitBreakerMap.putIfAbsent(name, circuitBreaker);
        if (ifAbsent != null) {
            circuitBreaker = null;
            return ifAbsent;
        }

        return circuitBreaker;
    }

    public void remove(String name) {
        circuitBreakerMap.remove(name);
    }

    public BigDecimal getFailureRate(String name) {
        CircuitBreaker circuitBreaker = circuitBreakerMap.get(name);
        return circuitBreaker == null ? new BigDecimal(0) : BigDecimal.valueOf(circuitBreaker.getMetrics().getFailureRate());
    }
}
