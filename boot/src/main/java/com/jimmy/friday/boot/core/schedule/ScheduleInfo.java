package com.jimmy.friday.boot.core.schedule;

import lombok.Data;

import java.io.Serializable;

@Data
public class ScheduleInfo implements Serializable {

    private String springBeanId;

    private String className;

    private String methodName;

    private String scheduleId;

    private String cron;
}
