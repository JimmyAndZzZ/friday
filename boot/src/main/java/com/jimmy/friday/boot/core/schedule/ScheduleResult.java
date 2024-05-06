package com.jimmy.friday.boot.core.schedule;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class ScheduleResult implements Serializable {

    private Long traceId;

    private Boolean isSuccess;

    private String errorMessage;

    private Long endDate;

    private ScheduleResult() {

    }

    public static ScheduleResult ok(Long traceId) {
        ScheduleResult scheduleResult = new ScheduleResult();
        scheduleResult.isSuccess = true;
        scheduleResult.traceId = traceId;
        scheduleResult.endDate = System.currentTimeMillis();
        return scheduleResult;
    }

    public static ScheduleResult error(String errorMessage, Long traceId) {
        ScheduleResult scheduleResult = new ScheduleResult();
        scheduleResult.traceId = traceId;
        scheduleResult.isSuccess = false;
        scheduleResult.errorMessage = errorMessage;
        scheduleResult.endDate = System.currentTimeMillis();
        return scheduleResult;
    }
}
