package com.jimmy.friday.framework.callback;

import cn.hutool.core.collection.CollUtil;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.core.schedule.ScheduleInfo;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.ServiceRegister;
import com.jimmy.friday.boot.message.schedule.ScheduleRegister;
import com.jimmy.friday.framework.base.Callback;
import com.jimmy.friday.framework.core.ConfigLoad;
import com.jimmy.friday.framework.schedule.ScheduleCenter;
import com.jimmy.friday.framework.utils.JsonUtil;
import io.netty.channel.ChannelHandlerContext;

import java.util.Collection;

public class ScheduleCallback implements Callback {

    private final ConfigLoad configLoad;

    private final ScheduleCenter scheduleCenter;

    public ScheduleCallback(ConfigLoad configLoad, ScheduleCenter scheduleCenter) {
        this.configLoad = configLoad;
        this.scheduleCenter = scheduleCenter;
    }

    @Override
    public void prepare(ChannelHandlerContext ctx) {
        Collection<ScheduleInfo> schedules = scheduleCenter.getSchedules();
        if (CollUtil.isNotEmpty(schedules)) {
            ScheduleRegister scheduleRegister = new ScheduleRegister();
            scheduleRegister.setApplicationId(configLoad.getId());
            scheduleRegister.setApplicationName(configLoad.getApplicationName());
            scheduleRegister.setScheduleInfos(schedules);
            ctx.writeAndFlush(new Event(EventTypeEnum.SCHEDULE_REGISTER, JsonUtil.toString(scheduleRegister)));
        }
    }

    @Override
    public void close() {

    }
}
