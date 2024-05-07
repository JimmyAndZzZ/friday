package com.jimmy.friday.center.action.schedule;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.schedule.ScheduleResult;
import com.jimmy.friday.center.Schedule;
import com.jimmy.friday.center.base.Action;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduleResultAction implements Action<ScheduleResult> {

    @Autowired
    private Schedule schedule;

    @Override
    public void action(ScheduleResult scheduleResult, ChannelHandlerContext channelHandlerContext) {
        schedule.callback(scheduleResult.getTraceId(), scheduleResult.getEndDate(), scheduleResult.getIsSuccess(), scheduleResult.getErrorMessage());
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.SCHEDULE_RESULT;
    }
}
