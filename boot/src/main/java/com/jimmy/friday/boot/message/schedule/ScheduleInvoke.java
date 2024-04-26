package com.jimmy.friday.boot.message.schedule;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ScheduleInvoke implements Message {

    private String scheduleId;

    private Long traceId;

    private Map<String, String> param = new HashMap<>();

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.SCHEDULE_INVOKE;
    }
}
