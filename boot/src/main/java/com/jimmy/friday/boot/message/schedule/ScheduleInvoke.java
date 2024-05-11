package com.jimmy.friday.boot.message.schedule;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ScheduleInvoke implements Message {

    private Long traceId;

    private String scheduleId;

    private String param;

    private Long timeout;

    private Integer retry;

    private Integer shardingNum;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.SCHEDULE_INVOKE;
    }
}
