package com.jimmy.friday.framework.schedule;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.schedule.ScheduleInfo;
import com.jimmy.friday.boot.exception.ScheduleException;

import java.util.Map;

public class ScheduleCenter {

    private final Map<String, ScheduleInfo> scheduleInfoMap = Maps.newHashMap();

    public void register(ScheduleInfo scheduleInfo) {
        String scheduleId = scheduleInfo.getScheduleId();

        if (scheduleInfoMap.put(scheduleId, scheduleInfo) != null) {
            throw new ScheduleException(scheduleId + "定时器重复定义");
        }



    }
}
