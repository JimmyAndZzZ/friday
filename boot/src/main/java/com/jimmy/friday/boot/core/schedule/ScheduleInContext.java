package com.jimmy.friday.boot.core.schedule;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

public class ScheduleInContext extends HashMap<String,String> {

    @Getter
    @Setter
    private String scheduleId;

    @Getter
    @Setter
    private Long traceId;
}
