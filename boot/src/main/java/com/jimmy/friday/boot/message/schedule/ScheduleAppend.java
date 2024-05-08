package com.jimmy.friday.boot.message.schedule;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.core.schedule.ScheduleInfo;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;

@Data
public class ScheduleAppend implements Message {

    private String applicationName;

    private ScheduleInfo scheduleInfo;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.SCHEDULE_APPEND;
    }
}
