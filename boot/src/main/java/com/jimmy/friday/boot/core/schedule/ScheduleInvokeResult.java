package com.jimmy.friday.boot.core.schedule;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class ScheduleInvokeResult implements Serializable {

    private Boolean isSuccess;

    private String errorMessage;

    private Long endDate;

    private ScheduleInvokeResult() {

    }

    public static ScheduleInvokeResult ok() {
        ScheduleInvokeResult scheduleInvokeResult = new ScheduleInvokeResult();
        scheduleInvokeResult.isSuccess = true;
        scheduleInvokeResult.endDate = System.currentTimeMillis();
        return scheduleInvokeResult;
    }

    public static ScheduleInvokeResult error(String errorMessage) {
        ScheduleInvokeResult scheduleInvokeResult = new ScheduleInvokeResult();
        scheduleInvokeResult.isSuccess = false;
        scheduleInvokeResult.errorMessage = errorMessage;
        scheduleInvokeResult.endDate = System.currentTimeMillis();
        return scheduleInvokeResult;
    }
}
