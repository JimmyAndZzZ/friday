package com.jimmy.friday.framework.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.core.gateway.InvokeParam;
import com.jimmy.friday.boot.core.schedule.ScheduleContext;
import com.jimmy.friday.boot.core.schedule.ScheduleInfo;
import com.jimmy.friday.boot.exception.ScheduleException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScheduleExecutor {

    private final Map<String, Object> instance = new HashMap<>();

    private final Map<String, Method> method = new HashMap<>();

    private ApplicationContext applicationContext;


    private void execute(ScheduleInfo scheduleInfo) {
        String className = scheduleInfo.getClassName();
        String scheduleId = scheduleInfo.getScheduleId();
        String methodName = scheduleInfo.getMethodName();
        String springBeanId = scheduleInfo.getSpringBeanId();



    }


    /**
     * 获取执行方法
     *
     * @param clazz
     * @param methodName
     * @return
     */
    private Method findMethod(Class<?> clazz, String methodName) {
        Method[] methods = clazz.getMethods();
        if (ArrayUtil.isEmpty(methods)) {
            return null;
        }

        for (Method m : methods) {
            if (methodName.equals(m.getName())) {
                Class<?>[] parameterTypes = m.getParameterTypes();
                if (parameterTypes.length == 1 && parameterTypes[0].equals(ScheduleContext.class)) {
                    return m;
                }
            }
        }

        return null;
    }

    /**
     * 获取实例
     *
     * @param clazz
     * @param springBeanId
     * @return
     */
    private Object getInstanceObject(Class<?> clazz, String springBeanId) {
        String name = clazz.getName();

        if (StrUtil.isNotEmpty(springBeanId)) {
            try {
                return applicationContext.getBean(springBeanId);
            } catch (BeansException ignored) {
                return null;
            }
        }

        Object o = instance.get(name);
        if (o != null) {
            return o;
        }

        try {
            Object bean = clazz.newInstance();
            instance.put(name, bean);
            return bean;
        } catch (Exception e) {
            return null;
        }
    }
}
