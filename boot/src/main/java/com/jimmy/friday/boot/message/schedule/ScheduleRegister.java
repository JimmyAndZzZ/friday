package com.jimmy.friday.boot.message.schedule;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.core.schedule.ScheduleInfo;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;

@Data
public class ScheduleRegister implements Message {

    private String applicationId;

    private String applicationName;

    private Collection<ScheduleInfo> scheduleInfos = new ArrayList<>();

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.SCHEDULE_REGISTER;
    }
}
