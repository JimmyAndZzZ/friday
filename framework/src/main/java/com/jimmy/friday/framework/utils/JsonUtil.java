package com.jimmy.friday.framework.utils;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.core.GenericTypeResolver;

import java.lang.reflect.Type;


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

    public static Object deserialize(String json, Type type, Class<?> sourceClass) {
        try {
            TypeFactory typeFactory = OBJECT_MAPPER.getTypeFactory();
            JavaType javaType = typeFactory.constructType(GenericTypeResolver.resolveType(type, sourceClass));
            return OBJECT_MAPPER.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
