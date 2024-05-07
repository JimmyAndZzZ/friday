package com.jimmy.friday.boot.message.schedule;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class ScheduleResult implements Message {

    private String id;

    private Long traceId;

    private String errorMessage;

    private Boolean isSuccess;

    private Long endDate;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.SCHEDULE_RESULT;
    }
}
