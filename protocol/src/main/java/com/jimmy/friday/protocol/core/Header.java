package com.jimmy.friday.protocol.core;

import com.google.common.collect.Maps;

import java.util.Map;

public class Header {

    private static ThreadLocal<Map<String, Object>> header = ThreadLocal.withInitial(() -> Maps.newHashMap());

    public static void putHeader(String key, Object value) {
        header.get().put(key, value);
    }

    public static void clearHeader() {
        header.get().clear();
    }

    public static Map<String, Object> getHeader() {
        return header.get();
    }

    public static Object getHeader(String key) {
        return header.get().get(key);
    }
}
