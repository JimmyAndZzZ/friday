package com.jimmy.friday.demo.core;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.core.schedule.ScheduleContext;
import com.jimmy.friday.boot.core.schedule.ScheduleInvokeResult;
import com.jimmy.friday.framework.annotation.schedule.Schedule;
import com.jimmy.friday.framework.base.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduleService implements InitializingBean {

    public ScheduleInvokeResult sharding(ScheduleContext scheduleContext) {
        log.info("sharding:{}", scheduleContext.getCurrentShardingNum());
        return ScheduleInvokeResult.ok();
    }

    @Schedule(id = "timeout43", cron = "0/2 * * * * ?", timeout = 100)
    public ScheduleInvokeResult timeout(ScheduleContext scheduleContext) {
        log.info("收到啦！！！！！");

        while (true) {
            ThreadUtil.sleep(100);

            if (StrUtil.isEmpty("!23")) {
                return ScheduleInvokeResult.ok();
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        com.jimmy.friday.framework.Schedule.append()
                .setScheduleId("ttt")
                .setCron("0/30 * * * * ?")
                .submit(new Job() {
                    @Override
                    public ScheduleInvokeResult run(ScheduleContext scheduleContext) {
                        return ScheduleInvokeResult.ok();
                    }
                }).append();
    }
}
