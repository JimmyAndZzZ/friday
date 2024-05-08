package com.jimmy.friday.boot.message.schedule;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.core.schedule.ScheduleInfo;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class ScheduleDelete implements Message {

    private String applicationName;

    private String scheduleId;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.SCHEDULE_DELETE;
    }
}
