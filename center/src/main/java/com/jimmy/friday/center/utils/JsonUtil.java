package com.jimmy.friday.center.utils;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonUtil() {

    }

    public static JsonNode parse(String json) {
        if (StrUtil.isEmpty(json)) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            return null;
        }
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
            log.error("json解析失败,json:{}", json, e);
            return null;
        }
    }
}
