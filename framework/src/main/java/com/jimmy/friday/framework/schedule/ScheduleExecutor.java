package com.jimmy.friday.framework.schedule;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.schedule.ScheduleContext;
import com.jimmy.friday.boot.core.schedule.ScheduleInfo;
import com.jimmy.friday.boot.core.schedule.ScheduleRunInfo;
import com.jimmy.friday.boot.exception.ScheduleException;
import com.jimmy.friday.boot.message.schedule.ScheduleResult;
import com.jimmy.friday.framework.support.TransmitSupport;
import com.jimmy.friday.framework.utils.ClassUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ScheduleExecutor {

    private final Map<String, Method> method = new HashMap<>();

    private final Map<String, Object> instance = new HashMap<>();

    private final ConcurrentMap<Long, RunningInfo> runningInfoMap = Maps.newConcurrentMap();

    private ScheduleCenter scheduleCenter;

    private TransmitSupport transmitSupport;

    private ApplicationContext applicationContext;

    public List<ScheduleRunInfo> getRunInfo() {
        if (MapUtil.isNotEmpty(runningInfoMap)) {
            return Lists.newArrayList();
        }

        List<ScheduleRunInfo> scheduleRunInfoList = Lists.newArrayList();

        for (Map.Entry<Long, RunningInfo> entry : runningInfoMap.entrySet()) {
            Long key = entry.getKey();
            RunningInfo value = entry.getValue();

            ScheduleRunInfo scheduleRunInfo = new ScheduleRunInfo();
            scheduleRunInfo.setScheduleId(value.getScheduleId());
            scheduleRunInfo.setRunTime(System.currentTimeMillis() - value.getStartDate());
            scheduleRunInfo.setTraceId(key);
            scheduleRunInfoList.add(scheduleRunInfo);
        }

        return scheduleRunInfoList;
    }

    public ScheduleExecutor(ScheduleCenter scheduleCenter, TransmitSupport transmitSupport, ApplicationContext applicationContext) {
        this.scheduleCenter = scheduleCenter;
        this.transmitSupport = transmitSupport;
        this.applicationContext = applicationContext;
    }

    public void interrupt(Long traceId) {
        RunningInfo runningInfo = runningInfoMap.remove(traceId);
        if (runningInfo == null) {
            log.error("traceId:{}中断失败，调度不存在", traceId);
            return;
        }

        runningInfo.getExecutorService().shutdownNow();
    }

    public void invoke(Long traceId, String scheduleId, String param) {
        ScheduleInfo scheduleInfo = scheduleCenter.getScheduleInfo(scheduleId);
        if (scheduleInfo == null) {
            ScheduleResult scheduleResult = new ScheduleResult();
            scheduleResult.setId(scheduleId);
            scheduleResult.setTraceId(traceId);
            scheduleResult.setIsSuccess(false);
            scheduleResult.setErrorMessage("本地定时器不存在");
            scheduleResult.setEndDate(System.currentTimeMillis());
            transmitSupport.send(scheduleResult);
            return;
        }

        RunningInfo runningInfo = new RunningInfo(scheduleId);
        //判断是否正在运行
        RunningInfo put = runningInfoMap.putIfAbsent(traceId, runningInfo);
        if (put != null) {
            log.error("traceId:{}调度正在运行，此次调度作废", traceId);

            runningInfo.close();
            return;
        }
        //执行定时器
        runningInfo.getExecutorService().submit(() -> {
            try {
                ScheduleContext scheduleContext = new ScheduleContext();
                scheduleContext.setScheduleId(scheduleId);
                scheduleContext.setParam(param);
                scheduleContext.setTraceId(traceId);

                com.jimmy.friday.boot.core.schedule.ScheduleResult execute = execute(scheduleInfo, scheduleContext);

                ScheduleResult scheduleResult = new ScheduleResult();
                scheduleResult.setId(scheduleId);
                scheduleResult.setTraceId(traceId);
                scheduleResult.setIsSuccess(execute.getIsSuccess());
                scheduleResult.setErrorMessage(execute.getErrorMessage());
                scheduleResult.setEndDate(execute.getEndDate());
                transmitSupport.send(scheduleResult);
            } finally {
                runningInfoMap.remove(traceId);
            }
        });
    }

    /**
     * 执行定时器
     *
     * @param scheduleInfo
     * @param scheduleContext
     */
    private com.jimmy.friday.boot.core.schedule.ScheduleResult execute(ScheduleInfo scheduleInfo, ScheduleContext scheduleContext) {
        Long traceId = scheduleContext.getTraceId();
        String className = scheduleInfo.getClassName();
        String scheduleId = scheduleInfo.getScheduleId();
        String methodName = scheduleInfo.getMethodName();
        String springBeanId = scheduleInfo.getSpringBeanId();

        Object instanceObject = this.getInstanceObject(className, springBeanId);
        if (instanceObject == null) {
            log.error("获取执行器实例失败,class:{}", className);
            return com.jimmy.friday.boot.core.schedule.ScheduleResult.error("获取执行器实例失败", traceId);
        }

        Method method = this.findMethod(className, methodName, scheduleId);
        if (method == null) {
            log.error("获取执行器方法失败,class:{},method:{}", className, methodName);
            return com.jimmy.friday.boot.core.schedule.ScheduleResult.error("获取执行器方法失败", traceId);
        }

        try {
            return (com.jimmy.friday.boot.core.schedule.ScheduleResult) method.invoke(instanceObject, scheduleContext);
        } catch (Throwable e) {
            log.error("执行失败", e);
            return com.jimmy.friday.boot.core.schedule.ScheduleResult.error(e.getMessage(), traceId);
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

    @Data
    private static class RunningInfo implements Serializable {

        private Long startDate;

        private String scheduleId;

        private ExecutorService executorService;

        public RunningInfo(String scheduleId) {
            this.scheduleId = scheduleId;
            this.startDate = System.currentTimeMillis();
            this.executorService = Executors.newSingleThreadExecutor();
        }

        public void close() {
            executorService.shutdownNow();
        }
    }
}
