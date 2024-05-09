package com.jimmy.friday.framework.callback;

import cn.hutool.core.collection.CollUtil;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.core.schedule.ScheduleInfo;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.exception.ScheduleException;
import com.jimmy.friday.boot.message.gateway.ServiceRegister;
import com.jimmy.friday.boot.message.schedule.ScheduleRegister;
import com.jimmy.friday.framework.base.Callback;
import com.jimmy.friday.framework.core.ConfigLoad;
import com.jimmy.friday.framework.schedule.ScheduleCenter;
import com.jimmy.friday.framework.utils.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.Collection;

@Slf4j
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
            scheduleRegister.setIp(this.getIpAddress());
            scheduleRegister.setScheduleInfos(schedules);
            ctx.writeAndFlush(new Event(EventTypeEnum.SCHEDULE_REGISTER, JsonUtil.toString(scheduleRegister)));
        }
    }

    @Override
    public void close() {

    }


    /**
     * 获取ip地址
     *
     * @return
     */
    private String getIpAddress() {
        try {
            InetAddress firstNonLoopBackAddress = configLoad.getLocalIpAddress();

            if (firstNonLoopBackAddress != null) {
                return firstNonLoopBackAddress.getHostAddress();
            }

            throw new ScheduleException("获取ip地址失败");
        } catch (Exception e) {
            log.error("获取ip地址失败", e);
            throw new ScheduleException("获取ip地址失败");
        }
    }
}
