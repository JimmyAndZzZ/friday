package com.jimmy.friday.center.core;

import cn.hutool.core.util.StrUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.other.GlobalConstants;
import com.jimmy.friday.boot.other.ShortUUID;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CommandSession {

    private final Cache<String, String> session = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES) // 设置过期时间为10分钟
            .build();

    public String getCurrent(String sessionKey) {
        String current = session.getIfPresent(sessionKey);
        if (StrUtil.isEmpty(current)) {
            throw new GatewayException("会话失效，请刷新页面");
        }

        session.put(sessionKey, current);
        return current;
    }

    public void cd(String sessionKey, String path) {
        session.put(sessionKey, path);
    }

    public String login() {
        String sessionKey = ShortUUID.uuid();
        session.put(sessionKey, GlobalConstants.Center.ROOT);
        return sessionKey;
    }

    public void exit(String sessionKey) {
        session.invalidate(sessionKey);
    }
}
