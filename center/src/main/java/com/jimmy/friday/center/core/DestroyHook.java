package com.jimmy.friday.center.core;

import cn.hutool.core.map.MapUtil;
import com.jimmy.friday.center.base.Close;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class DestroyHook {

    @Autowired
    private ApplicationContext applicationContext;

    @PreDestroy
    public void closeWhenDestroy() {
        log.info("正在关闭容器");

        Map<String, Close> beansOfType = applicationContext.getBeansOfType(Close.class);
        if (MapUtil.isNotEmpty(beansOfType)) {
            beansOfType.values().forEach(Close::close);
        }
    }
}
