package com.jimmy.friday.framework.utils;

import com.google.common.collect.Maps;
import org.springframework.cglib.proxy.Enhancer;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class ClassUtil {

    private static final Map<String, Class<?>> CLASS_CACHE = Maps.newHashMap();

    private ClassUtil() {

    }

    public static Class<?> loadClass(String className) throws ClassNotFoundException {
        Class<?> clazz = CLASS_CACHE.get(className);
        if (clazz != null) {
            return clazz;
        }

        clazz = Class.forName(className);
        CLASS_CACHE.put(className, clazz);
        return clazz;
    }

    public static boolean classEquals(Class<?> source, Class<?> target) {
        if (source == null || target == null) {
            return true;
        }

        return getWrapperClass(source).equals(getWrapperClass(target));
    }

    /**
     * 判断类是否相同
     *
     * @param className1
     * @param className2
     * @return
     */
    public static boolean classEqual(String className1, String className2) {
        return className1.equals(className2) || (isPrimitive(className1) && getWrapperClassName(className1).equals(className2)) || (isPrimitive(className2) && getWrapperClassName(className2).equals(className1));
    }

    /**
     * 判断是否为基本类
     *
     * @param className
     * @return
     */
    public static boolean isPrimitive(String className) {
        return className.equals("int") ||
                className.equals("boolean") ||
                className.equals("byte") ||
                className.equals("char") ||
                className.equals("short") ||
                className.equals("long") ||
                className.equals("float") ||
                className.equals("double");
    }

    /**
     * 获取包装类名
     *
     * @param primitiveTypeName
     * @return
     */
    public static String getWrapperClassName(String primitiveTypeName) {
        switch (primitiveTypeName) {
            case "int":
                return Integer.class.getName();
            case "boolean":
                return Boolean.class.getName();
            case "byte":
                return Byte.class.getName();
            case "char":
                return Character.class.getName();
            case "short":
                return Short.class.getName();
            case "long":
                return Long.class.getName();
            case "float":
                return Float.class.getName();
            case "double":
                return Double.class.getName();
            default:
                return primitiveTypeName;
        }
    }

    /**
     * 获取参数类
     *
     * @param clazz
     * @return
     * @throws Exception
     */
    public static Class<?> getWrapperClass(Class<?> clazz) {
        String paramType = clazz.getName();

        switch (paramType) {
            case "void":
                return Void.class;
            case "int":
                return Integer.TYPE;
            case "byte":
                return Byte.TYPE;
            case "short":
                return Short.TYPE;
            case "long":
                return Long.TYPE;
            case "float":
                return Float.TYPE;
            case "double":
                return Double.TYPE;
            case "char":
                return Character.TYPE;
            case "boolean":
                return Boolean.TYPE;
            default:
                return clazz;
        }
    }

    /**
     * 获取class类
     *
     * @param clazz
     * @return
     */
    public static Class<?> getClass(Class<?> clazz) {
        if (Enhancer.isEnhanced(clazz)) {
            return getClass(clazz.getSuperclass());
        }

        return clazz;
    }
}
