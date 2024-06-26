package com.jimmy.friday.framework.base;

import com.jimmy.friday.boot.core.schedule.ScheduleContext;
import com.jimmy.friday.boot.core.schedule.ScheduleInvokeResult;

public interface Job {

    ScheduleInvokeResult run(ScheduleContext scheduleContext);
}
