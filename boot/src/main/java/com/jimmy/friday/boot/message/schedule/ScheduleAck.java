package com.jimmy.friday.boot.message.schedule;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class ScheduleAck implements Message {

    private String id;

    private Long traceId;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.SCHEDULE_ACK;
    }
}
