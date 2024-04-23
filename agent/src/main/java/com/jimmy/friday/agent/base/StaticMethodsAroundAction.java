package com.jimmy.friday.agent.base;

import java.lang.reflect.Method;

public interface StaticMethodsAroundAction {

    void beforeMethod(Class clazz, Method method, Class<?>[] parameterTypes, Object[] param);

    void afterMethod(Class clazz, Method method, Class<?>[] parameterTypes, Object[] param, Object result, Long cost);

    void handleMethodException(Class clazz, Method method, Class<?>[] parameterTypes, Object[] param, Throwable throwable, Long cost);

    default void ultimate(Class clazz, Method method, Class<?>[] parameterTypes, Object[] param) {

    }
}
