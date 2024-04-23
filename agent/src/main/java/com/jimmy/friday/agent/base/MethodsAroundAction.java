package com.jimmy.friday.agent.base;

import com.jimmy.friday.agent.bytebuddy.EnhancedInstance;

import java.lang.reflect.Method;

public interface MethodsAroundAction {

    void beforeMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param);

    void afterMethod(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Object result, Long cost);

    void handleMethodException(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param, Throwable throwable, Long cost);

    default void ultimate(EnhancedInstance enhancedInstance, Method method, Class<?>[] parameterTypes, Object[] param) {

    }
}
