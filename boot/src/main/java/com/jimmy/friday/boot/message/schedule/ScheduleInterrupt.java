package com.jimmy.friday.boot.message.schedule;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class ScheduleInterrupt implements Message {

    private Long traceId;

    private String scheduleId;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.SCHEDULE_INTERRUPT;
    }
}
