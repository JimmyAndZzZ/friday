package com.jimmy.friday.framework.schedule;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.core.schedule.ScheduleContext;
import com.jimmy.friday.boot.core.schedule.ScheduleInfo;
import com.jimmy.friday.boot.core.schedule.ScheduleResult;
import com.jimmy.friday.boot.exception.ScheduleException;
import com.jimmy.friday.framework.utils.ClassUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ScheduleExecutor {

    private final Map<String, Method> method = new HashMap<>();

    private final Map<String, Object> instance = new HashMap<>();

    private ApplicationContext applicationContext;

    private void execute(ScheduleInfo scheduleInfo, ScheduleContext scheduleContext, Long traceId) {
        String className = scheduleInfo.getClassName();
        String scheduleId = scheduleInfo.getScheduleId();
        String methodName = scheduleInfo.getMethodName();
        String springBeanId = scheduleInfo.getSpringBeanId();

        Object instanceObject = this.getInstanceObject(className, springBeanId);
        if (instanceObject == null) {
            log.error("获取执行器实例失败,class:{}", className);
            return;
        }

        Method method = this.findMethod(className, methodName, scheduleId);
        if (method == null) {
            log.error("获取执行器方法失败,class:{},method:{}", className, methodName);
            return;
        }

        try {
            method.invoke(instanceObject, scheduleContext);
        } catch (Exception e) {
            log.error("执行失败", e);
        }
    }


    /**
     * 获取执行方法
     *
     * @param className
     * @param methodName
     * @return
     */
    private Method findMethod(String className, String methodName, String scheduleId) {
        Method method = this.method.get(scheduleId);
        if (method != null) {
            return method;
        }

        Class<?> clazz;
        try {
            clazz = ClassUtil.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new ScheduleException(className + "类初始化失败");
        }

        Method[] methods = clazz.getMethods();
        if (ArrayUtil.isEmpty(methods)) {
            return null;
        }

        for (Method m : methods) {
            if (methodName.equals(m.getName())) {
                Class<?> returnType = m.getReturnType();
                Class<?>[] parameterTypes = m.getParameterTypes();
                if (parameterTypes.length == 1 && parameterTypes[0].equals(ScheduleContext.class) && returnType.equals(ScheduleResult.class)) {
                    this.method.put(scheduleId, m);
                    return m;
                }
            }
        }

        return null;
    }

    /**
     * 获取实例
     *
     * @param className
     * @param springBeanId
     * @return
     */
    private Object getInstanceObject(String className, String springBeanId) {
        if (StrUtil.isNotEmpty(springBeanId)) {
            try {
                Object bean = applicationContext.getBean(springBeanId);
                instance.put(className, bean);
            } catch (BeansException ignored) {
                return null;
            }
        }

        Object o = instance.get(className);
        if (o != null) {
            return o;
        }

        try {
            Class<?> clazz = ClassUtil.loadClass(className);

            Object bean = clazz.newInstance();
            instance.put(className, bean);
            return bean;
        } catch (ClassNotFoundException e) {
            throw new ScheduleException(className + "类不存在");
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ScheduleException(className + "类初始化失败");
        }
    }
}
