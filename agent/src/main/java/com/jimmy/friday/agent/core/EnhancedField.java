package com.jimmy.friday.agent.core;

import com.google.common.collect.Maps;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class EnhancedField implements Serializable {

    private Object dynamic;

    private String cmd;

    private Boolean isNeedTrace = false;

    private Boolean isNeedMonitor = false;

    private Boolean isNeedWatch = false;

    private AtomicInteger traceCount = new AtomicInteger(0);

    private AtomicInteger monitorCount = new AtomicInteger(0);

    private Map<String, Object> attachments = Maps.newHashMap();

    public EnhancedField() {

    }

    public <T> T getAttachment(String key, Class<T> clazz) {
        Object o = this.attachments.get(key);
        return o == null ? null : clazz.cast(o);
    }

    public void setAttachment(String key, Object value) {
        this.attachments.put(key, value);
    }

    public EnhancedField(Object dynamic) {
        this.dynamic = dynamic;
    }
}
