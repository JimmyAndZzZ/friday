package com.jimmy.friday.boot.core.schedule;

import lombok.Data;

import java.io.Serializable;

@Data
public class ScheduleRunInfo implements Serializable {

    private String scheduleId;

    private Long runTime;

    private Long traceId;

    private String applicationId;

}
