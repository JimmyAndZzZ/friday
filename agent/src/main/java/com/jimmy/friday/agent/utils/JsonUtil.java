package com.jimmy.friday.agent.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;


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

    public static <T> T toBean(String json, Class<T> clazz) {
        if (Strings.isNullOrEmpty(json)) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
