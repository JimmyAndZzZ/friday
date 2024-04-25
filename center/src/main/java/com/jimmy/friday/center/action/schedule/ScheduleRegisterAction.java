package com.jimmy.friday.center.action.schedule;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.schedule.ScheduleRegister;
import com.jimmy.friday.center.base.Action;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduleRegisterAction implements Action<ScheduleRegister> {

    @Override
    public void action(ScheduleRegister scheduleRegister, ChannelHandlerContext channelHandlerContext) {

    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.SCHEDULE_REGISTER;
    }
}
