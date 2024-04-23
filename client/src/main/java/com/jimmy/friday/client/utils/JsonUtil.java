package com.jimmy.friday.client.utils;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonUtil() {

    }

    public static String toString(Object o) {
        if (o == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(o);
        } catch (Exception e) {
            return o.toString();
        }
    }

    public static <T> T parseObject(String json, Class<T> clazz) {
        if (StrUtil.isEmpty(json)) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
