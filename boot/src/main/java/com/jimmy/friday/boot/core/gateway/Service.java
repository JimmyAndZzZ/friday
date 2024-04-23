package com.jimmy.friday.boot.core.gateway;

import com.jimmy.friday.boot.enums.ServiceStatusEnum;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class Service implements Serializable {

    private String serviceId;

    private String applicationId;

    private String version;

    private String name;

    private String ipAddress;

    private Integer port;

    private String type;

    private Integer weight = 0;

    private String group;

    private AtomicInteger referenceCount = new AtomicInteger(0);

    private List<Method> methods = new ArrayList<>();

    private Map<String, Object> attribute = new HashMap<>();

    private ServiceStatusEnum status = ServiceStatusEnum.ALIVE;

    public void putAttribute(String key, Object value) {
        this.attribute.put(key, value);
    }

    public Object getAttribute(String key) {
        return this.attribute.get(key);
    }

    public ServiceTypeEnum serviceType() {
        return ServiceTypeEnum.queryByType(type);
    }

    public String getStringAttribute(String key) {
        Object attribute = this.getAttribute(key);
        return attribute != null ? attribute.toString() : null;
    }

    public <T> T getAttributeByClass(String key, Class<T> clazz) {
        Object attribute = this.getAttribute(key);

        if (attribute == null) {
            return null;
        }

        if (clazz.isInstance(attribute)) {
            return (T) attribute;
        }

        throw new ClassCastException();
    }

    public void release() {
        this.referenceCount.decrementAndGet();
    }

    public void use() {
        this.referenceCount.incrementAndGet();
    }
}
