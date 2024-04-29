package com.jimmy.friday.boot.message.schedule;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.core.schedule.ScheduleRunInfo;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

import java.util.*;

@Data
public class ScheduleHeartbeat implements Message {

    private String applicationId;

    private List<ScheduleRunInfo> scheduleRunInfoList = new ArrayList<>();

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.SCHEDULE_HEARTBEAT;
    }
}
