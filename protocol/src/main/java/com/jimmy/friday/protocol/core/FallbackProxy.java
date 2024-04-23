package com.jimmy.friday.protocol.core;

import com.jimmy.friday.protocol.base.Output;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FallbackProxy extends HystrixCommand<Object> {

    private Output send;

    private Output fallback;

    private String message;

    public FallbackProxy(String name, Output send, Output fallback, String message) {
        super(HystrixCommandGroupKey.Factory.asKey(name));
        this.send = send;
        this.message = message;
        this.fallback = fallback;
    }

    @Override
    public Object run() throws Exception {
        return send.send(message);
    }

    @Override
    public Object getFallback() {
        log.error("熔断降级,body:{}", message);
        return fallback.send(message);
    }
}
