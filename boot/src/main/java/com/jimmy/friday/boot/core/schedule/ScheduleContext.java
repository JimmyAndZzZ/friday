package com.jimmy.friday.boot.core.schedule;

import lombok.Data;

import java.io.Serializable;

@Data
public class ScheduleContext implements Serializable {

    private String scheduleId;

    private Long traceId;

    private String param;
}
