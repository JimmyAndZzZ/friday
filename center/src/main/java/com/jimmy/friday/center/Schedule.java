package com.jimmy.friday.center;

import cn.hutool.core.util.IdUtil;
import com.jimmy.friday.boot.core.schedule.ScheduleResult;
import com.jimmy.friday.boot.message.schedule.ScheduleInvoke;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class Schedule {

    public ScheduleResult submit(String scheduleId, Map<String, String> param) {
        Long traceId = IdUtil.getSnowflake(1, 1).nextId();

        ScheduleInvoke scheduleInvoke = new ScheduleInvoke();
        scheduleInvoke.setScheduleId(scheduleId);
        scheduleInvoke.setTraceId(traceId);
        scheduleInvoke.setParam(param);


        return null;
    }
}
