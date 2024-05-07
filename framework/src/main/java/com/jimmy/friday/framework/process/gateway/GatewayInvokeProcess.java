package com.jimmy.friday.framework.process.gateway;

import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.GatewayInvoke;
import com.jimmy.friday.framework.base.Process;
import com.jimmy.friday.framework.support.InvokeSupport;
import com.jimmy.friday.framework.utils.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;

public class GatewayInvokeProcess implements Process<GatewayInvoke> {

    @Autowired
    private InvokeSupport invokeSupport;

    @Override
    public void process(GatewayInvoke gatewayInvoke, ChannelHandlerContext ctx) {
        invokeSupport.invoke(gatewayInvoke);
        ctx.writeAndFlush(new Event(EventTypeEnum.GATEWAY_INVOKE, JsonUtil.toString(gatewayInvoke)));
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.GATEWAY_INVOKE;
    }
}
