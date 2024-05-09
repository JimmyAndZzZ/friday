package com.jimmy.friday.framework.other;

import com.jimmy.friday.boot.core.schedule.ScheduleContext;
import com.jimmy.friday.boot.core.schedule.ScheduleResult;
import com.jimmy.friday.framework.base.Job;
import lombok.Data;

@Data
public class JobProxy {

    private Job job;

    public ScheduleResult run(ScheduleContext scheduleContext){
        return job.run(scheduleContext);
    }

}
