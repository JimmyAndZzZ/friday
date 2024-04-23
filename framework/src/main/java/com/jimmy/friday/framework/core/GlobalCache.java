package com.jimmy.friday.framework.core;

import cn.hutool.core.map.MapUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalCache {

    private final ConcurrentHashMap<String, Object> singlePropCache = new ConcurrentHashMap<>(64);

    private final ConcurrentHashMap<String, Map<String, Object>> cache = new ConcurrentHashMap<>(64);

    public void put(String mainKey, String key, Object attachment) {
        Objects.requireNonNull(mainKey);
        Objects.requireNonNull(key);
        Objects.requireNonNull(attachment);

        Map<String, Object> map = new HashMap<>();
        map.put(key, attachment);

        Map<String, Object> put = cache.putIfAbsent(mainKey, map);
        if (put != null) {
            put.put(key, attachment);
        }
    }

    public void put(String key, Object attachment) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(attachment);
        singlePropCache.put(key, attachment);
    }

    public <T> T putIfAbsent(String mainKey, String key, T attachment) {
        Objects.requireNonNull(mainKey);
        Objects.requireNonNull(key);
        Objects.requireNonNull(attachment);

        Map<String, Object> map = new HashMap<>();
        map.put(key, attachment);

        Map<String, Object> put = cache.putIfAbsent(mainKey, map);
        return put != null ? (T) put.putIfAbsent(key, attachment) : null;
    }

    public <T> T putIfAbsent(String key, T attachment) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(attachment);
        return (T) singlePropCache.putIfAbsent(key, attachment);
    }

    public Object get(String key) {
        return singlePropCache.get(key);
    }

    public Object get(String mainKey, String key) {
        Map<String, Object> map = cache.get(mainKey);
        return map != null ? map.get(key) : null;
    }

    public <T> T get(String mainKey, String key, Class<T> clazz) {
        Map<String, Object> map = cache.get(mainKey);
        return map != null ? MapUtil.get(map, key, clazz) : null;
    }

    public Object removeSingle(String key) {
        return singlePropCache.remove(key);
    }

    public Object remove(String key) {
        return cache.remove(key);
    }

    public Object remove(String mainKey, String key) {
        Map<String, Object> map = cache.get(mainKey);
        return map != null ? map.remove(key) : null;
    }

    public <T> T get(String key, Class<T> clazz) {
        return MapUtil.get(singlePropCache, key, clazz);
    }
}
