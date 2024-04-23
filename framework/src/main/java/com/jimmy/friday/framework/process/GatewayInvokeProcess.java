package com.jimmy.friday.framework.process;

import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.GatewayInvoke;
import com.jimmy.friday.framework.base.Process;
import com.jimmy.friday.framework.support.InvokeSupport;
import com.jimmy.friday.framework.utils.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;

public class GatewayInvokeProcess implements Process {

    @Autowired
    private InvokeSupport invokeSupport;

    @Override
    public void process(Event event, ChannelHandlerContext ctx) {
        String message = event.getMessage();
        GatewayInvoke gatewayInvoke = JsonUtil.parseObject(message, GatewayInvoke.class);
        invokeSupport.invoke(gatewayInvoke);

        event.setMessage(JsonUtil.toString(gatewayInvoke));
        ctx.writeAndFlush(event);
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.GATEWAY_INVOKE;
    }
}
