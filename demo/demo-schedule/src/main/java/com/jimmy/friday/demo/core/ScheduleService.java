package com.jimmy.friday.demo.core;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.core.schedule.ScheduleContext;
import com.jimmy.friday.boot.core.schedule.ScheduleInvokeResult;
import com.jimmy.friday.framework.annotation.schedule.Schedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduleService {

    //@Schedule(id = "sharding", cron = "0/30 * * * * ?", shardingNum = 3)
    public ScheduleInvokeResult sharding(ScheduleContext scheduleContext) {
        log.info("sharding:{}", scheduleContext.getCurrentShardingNum());
        return ScheduleInvokeResult.ok();
    }

    @Schedule(id = "timeout", cron = "0/2 * * * * ?", timeout = 10)
    public ScheduleInvokeResult timeout(ScheduleContext scheduleContext) {
        while (true) {
            ThreadUtil.sleep(100);

            if (StrUtil.isEmpty("!23")) {
                return ScheduleInvokeResult.ok();
            }
        }
    }
}
