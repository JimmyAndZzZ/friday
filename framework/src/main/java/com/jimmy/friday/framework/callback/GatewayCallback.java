package com.jimmy.friday.framework.callback;

import cn.hutool.core.collection.CollUtil;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.ChannelSub;
import com.jimmy.friday.boot.message.gateway.ServiceDestroy;
import com.jimmy.friday.boot.message.gateway.ServiceRegister;
import com.jimmy.friday.framework.base.Callback;
import com.jimmy.friday.framework.core.ConfigLoad;
import com.jimmy.friday.framework.support.ChannelSupport;
import com.jimmy.friday.framework.support.RegisterSupport;
import com.jimmy.friday.framework.support.TransmitSupport;
import com.jimmy.friday.framework.utils.JsonUtil;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

public class GatewayCallback implements Callback {

    private ConfigLoad configLoad;

    private ChannelSupport channelSupport;

    private RegisterSupport registerSupport;

    private TransmitSupport transmitSupport;

    public GatewayCallback(RegisterSupport registerSupport, ConfigLoad configLoad, TransmitSupport transmitSupport, ChannelSupport channelSupport) {
        this.configLoad = configLoad;
        this.channelSupport = channelSupport;
        this.registerSupport = registerSupport;
        this.transmitSupport = transmitSupport;
    }

    @Override
    public void prepare(ChannelHandlerContext ctx) {
        List<Service> services = registerSupport.getServices();
        if (CollUtil.isNotEmpty(services)) {
            ctx.writeAndFlush(new Event(EventTypeEnum.SERVICE_REGISTER, JsonUtil.toString(new ServiceRegister(configLoad.getId(), services))));
        }

        List<ChannelSub> channelSubList = channelSupport.getChannelSubList();
        if (CollUtil.isNotEmpty(channelSubList)) {
            for (ChannelSub channelSub : channelSubList) {
                ctx.writeAndFlush(new Event(EventTypeEnum.CHANNEL_SUB, JsonUtil.toString(channelSub)));
            }
        }
    }

    @Override
    public void close() {
        List<Service> services = registerSupport.getServices();
        if (CollUtil.isNotEmpty(services)) {
            transmitSupport.broadcast(new ServiceDestroy(configLoad.getId(), services));
        }
    }
}
