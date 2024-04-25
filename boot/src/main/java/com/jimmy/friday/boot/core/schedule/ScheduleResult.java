package com.jimmy.friday.boot.core.schedule;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class ScheduleResult implements Serializable {

    private Boolean isSuccess;

    private String errorMessage;

    private ScheduleResult() {

    }

    public static ScheduleResult ok() {
        ScheduleResult scheduleResult = new ScheduleResult();
        scheduleResult.isSuccess = true;
        return scheduleResult;
    }

    public static ScheduleResult error(String errorMessage) {
        ScheduleResult scheduleResult = new ScheduleResult();
        scheduleResult.isSuccess = false;
        scheduleResult.errorMessage = errorMessage;
        return scheduleResult;
    }
}
