package com.jimmy.friday.framework.process.gateway;

import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.Heartbeat;
import com.jimmy.friday.framework.base.Process;
import com.jimmy.friday.framework.utils.JsonUtil;
import io.netty.channel.ChannelHandlerContext;

public class GatewayHeartbeatProcess implements Process<Heartbeat> {

    @Override
    public void process(Heartbeat heartbeat, ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new Event(EventTypeEnum.GATEWAY_HEARTBEAT, JsonUtil.toString(heartbeat)));
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.GATEWAY_HEARTBEAT;
    }
}
