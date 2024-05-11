package com.jimmy.friday.framework.process.schedule;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.schedule.ScheduleInvoke;
import com.jimmy.friday.framework.base.Process;
import com.jimmy.friday.framework.schedule.ScheduleExecutor;
import io.netty.channel.ChannelHandlerContext;

public class ScheduleInvokeProcess implements Process<ScheduleInvoke> {

    private ScheduleExecutor scheduleExecutor;

    public ScheduleInvokeProcess(ScheduleExecutor scheduleExecutor) {
        this.scheduleExecutor = scheduleExecutor;
    }

    @Override
    public void process(ScheduleInvoke scheduleInvoke, ChannelHandlerContext ctx) {
        scheduleExecutor.invoke(
                scheduleInvoke.getTraceId(),
                scheduleInvoke.getScheduleId(),
                scheduleInvoke.getParam(),
                scheduleInvoke.getTimeout(),
                scheduleInvoke.getRetry());
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.SCHEDULE_INVOKE;
    }
}
