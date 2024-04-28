package com.jimmy.friday.boot.core.schedule;

import lombok.Data;

import java.io.Serializable;

@Data
public class ScheduleRunInfo implements Serializable {

    private String scheduleId;

    private Integer runTime;

    private Long traceId;

}
