package com.jimmy.friday.framework.schedule;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.schedule.ScheduleInfo;
import com.jimmy.friday.boot.exception.ScheduleException;

import java.util.Map;

public class ScheduleCenter {

    private final Map<String, ScheduleInfo> scheduleInfoMap = Maps.newHashMap();

    public void register(String className, String methodName, String scheduleId) {
        ScheduleInfo scheduleInfo = new ScheduleInfo();
        scheduleInfo.setScheduleId(scheduleId);
        scheduleInfo.setMethodName(methodName);
        scheduleInfo.setClassName(className);

        if (scheduleInfoMap.put(scheduleId, scheduleInfo) != null) {
            throw new ScheduleException(scheduleId + "定时器重复定义");
        }
    }

    public void setSpringBeanId(String scheduleId, String springBeanId) {
        ScheduleInfo scheduleInfo = scheduleInfoMap.get(scheduleId);
        if (scheduleInfo == null) {
            throw new ScheduleException(scheduleId + "定时器不存在");
        }

        scheduleInfo.setSpringBeanId(springBeanId);
    }

    
}
