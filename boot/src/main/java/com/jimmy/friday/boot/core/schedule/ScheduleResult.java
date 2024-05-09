package com.jimmy.friday.boot.core.schedule;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
public class ScheduleResult implements Serializable {

    private Boolean isSuccess;

    private String errorMessage;

    private Long endDate;

    private ScheduleResult() {

    }

    public static ScheduleResult ok() {
        ScheduleResult scheduleResult = new ScheduleResult();
        scheduleResult.isSuccess = true;
        scheduleResult.endDate = System.currentTimeMillis();
        return scheduleResult;
    }

    public static ScheduleResult error(String errorMessage) {
        ScheduleResult scheduleResult = new ScheduleResult();
        scheduleResult.isSuccess = false;
        scheduleResult.errorMessage = errorMessage;
        scheduleResult.endDate = System.currentTimeMillis();
        return scheduleResult;
    }
}
