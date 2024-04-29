package com.jimmy.friday.center.action.schedule;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.schedule.ScheduleRegister;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.core.schedule.ScheduleCenter;
import com.jimmy.friday.center.core.schedule.ScheduleSession;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduleRegisterAction implements Action<ScheduleRegister> {

    @Autowired
    private ScheduleCenter scheduleCenter;

    @Autowired
    private ScheduleSession scheduleSession;

    @Override
    public void action(ScheduleRegister scheduleRegister, ChannelHandlerContext channelHandlerContext) {
        scheduleSession.connect(scheduleRegister.getApplicationId(), scheduleRegister.getApplicationName(), scheduleRegister.getIp());
        scheduleCenter.register(scheduleRegister.getScheduleInfos(), scheduleRegister.getApplicationName());
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.SCHEDULE_REGISTER;
    }
}
