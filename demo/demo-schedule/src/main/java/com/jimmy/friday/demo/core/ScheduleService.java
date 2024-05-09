package com.jimmy.friday.demo.core;

import com.jimmy.friday.boot.core.schedule.ScheduleContext;
import com.jimmy.friday.boot.core.schedule.ScheduleResult;
import com.jimmy.friday.framework.annotation.Schedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduleService {

    @Schedule(id = "test", cron = "0/30 * * * * ?")
    public ScheduleResult run(ScheduleContext scheduleContext) {
        log.info("收到定时器:{}", scheduleContext.getTraceId());
        return ScheduleResult.ok();
    }
}
