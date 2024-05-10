package com.jimmy.friday.demo.core;

import com.jimmy.friday.boot.core.schedule.ScheduleContext;
import com.jimmy.friday.boot.core.schedule.ScheduleInvokeResult;
import com.jimmy.friday.framework.annotation.schedule.Schedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduleService {

    @Schedule(id = "test", cron = "0/30 * * * * ?")
    public ScheduleInvokeResult run(ScheduleContext scheduleContext) {
        log.info("收到定时器:{}", scheduleContext.getTraceId());
        return ScheduleInvokeResult.ok();
    }

    @Schedule(id = "run1", cron = "0/3 * * * * ?")
    public ScheduleInvokeResult run1(ScheduleContext scheduleContext) {
        log.info("run1:{}", scheduleContext.getTraceId());
        return ScheduleInvokeResult.ok();
    }
}
