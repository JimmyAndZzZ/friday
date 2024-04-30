package com.jimmy.friday.center.action.schedule;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.schedule.ScheduleHeartbeat;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.core.schedule.ScheduleSession;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduleHeartbeatAction implements Action<ScheduleHeartbeat> {

    @Autowired
    private ScheduleSession scheduleSession;

    @Override
    public void action(ScheduleHeartbeat scheduleHeartbeat, ChannelHandlerContext channelHandlerContext) {
        scheduleSession.heartbeat(scheduleHeartbeat.getApplicationId(), scheduleHeartbeat.getApplicationName(), scheduleHeartbeat.getScheduleRunInfoList());
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.SCHEDULE_HEARTBEAT;
    }
}
