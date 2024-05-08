package com.jimmy.friday.center.action.schedule;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.schedule.ScheduleAppend;
import com.jimmy.friday.boot.message.schedule.ScheduleResult;
import com.jimmy.friday.center.Schedule;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.core.schedule.ScheduleCenter;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduleAppendAction implements Action<ScheduleAppend> {

    @Autowired
    private ScheduleCenter scheduleCenter;

    @Override
    public void action(ScheduleAppend scheduleAppend, ChannelHandlerContext channelHandlerContext) {
        scheduleCenter.append(scheduleAppend.getScheduleInfo(), scheduleAppend.getApplicationName());
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.SCHEDULE_APPEND;
    }
}
