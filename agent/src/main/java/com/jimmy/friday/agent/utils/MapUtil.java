package com.jimmy.friday.agent.utils;

import java.util.Map;

public class MapUtil {

    private MapUtil() {

    }

    public static <T> T get(Map<String, Object> map, String key, Class<T> clazz) {
        Object o = map.get(key);
        if (o == null) {
            return null;
        }

        if (clazz.isInstance(o)) {
            return clazz.cast(o);
        } else {
            throw new IllegalArgumentException("Object is not of the expected type.");
        }
    }

    public static String getString(Map<String, Object> map, String key) {
        Object o = map.get(key);
        return o == null ? null : o.toString();
    }
}
