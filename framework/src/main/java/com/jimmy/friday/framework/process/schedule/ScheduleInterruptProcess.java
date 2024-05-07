package com.jimmy.friday.framework.process.schedule;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.schedule.ScheduleInterrupt;
import com.jimmy.friday.framework.base.Process;
import com.jimmy.friday.framework.schedule.ScheduleExecutor;
import io.netty.channel.ChannelHandlerContext;

public class ScheduleInterruptProcess implements Process<ScheduleInterrupt> {

    private ScheduleExecutor scheduleExecutor;

    public ScheduleInterruptProcess(ScheduleExecutor scheduleExecutor) {
        this.scheduleExecutor = scheduleExecutor;
    }

    @Override
    public void process(ScheduleInterrupt scheduleInterrupt, ChannelHandlerContext ctx) {
        scheduleExecutor.interrupt(scheduleInterrupt.getTraceId());
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.SCHEDULE_INTERRUPT;
    }
}
