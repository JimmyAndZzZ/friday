package com.jimmy.friday.framework.process.gateway;

import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.framework.base.Process;
import io.netty.channel.ChannelHandlerContext;

public class GatewayHeartbeatProcess implements Process {

    @Override
    public void process(Event event, ChannelHandlerContext ctx) {
        ctx.writeAndFlush(event);
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.GATEWAY_HEARTBEAT;
    }
}
