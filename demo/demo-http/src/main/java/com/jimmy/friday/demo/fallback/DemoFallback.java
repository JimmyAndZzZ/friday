package com.jimmy.friday.demo.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DemoFallback {

    public String bcFallback(Long id, String str, String ss) {
        log.info("收到bc:{},{},{}", id, str, ss);
        return "fallback";
    }
}
