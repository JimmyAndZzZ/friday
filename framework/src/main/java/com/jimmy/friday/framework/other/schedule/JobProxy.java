package com.jimmy.friday.framework.other.schedule;

import com.jimmy.friday.boot.core.schedule.ScheduleContext;
import com.jimmy.friday.boot.core.schedule.ScheduleInvokeResult;
import com.jimmy.friday.framework.base.Job;
import lombok.Data;

@Data
public class JobProxy {

    private Job job;

    public ScheduleInvokeResult run(ScheduleContext scheduleContext){
        return job.run(scheduleContext);
    }

}
