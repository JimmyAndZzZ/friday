package com.jimmy.friday.framework.base;

import com.jimmy.friday.boot.core.schedule.ScheduleContext;
import com.jimmy.friday.boot.core.schedule.ScheduleResult;

public interface Job {

    ScheduleResult run(ScheduleContext scheduleContext);
}
