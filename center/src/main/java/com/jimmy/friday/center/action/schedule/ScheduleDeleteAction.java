package com.jimmy.friday.center.action.schedule;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.schedule.ScheduleAppend;
import com.jimmy.friday.boot.message.schedule.ScheduleDelete;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.core.schedule.ScheduleCenter;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduleDeleteAction implements Action<ScheduleDelete> {

    @Autowired
    private ScheduleCenter scheduleCenter;

    @Override
    public void action(ScheduleDelete scheduleDelete, ChannelHandlerContext channelHandlerContext) {
        scheduleCenter.delete(scheduleDelete.getScheduleId(), scheduleDelete.getApplicationName());
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.SCHEDULE_APPEND;
    }
}
