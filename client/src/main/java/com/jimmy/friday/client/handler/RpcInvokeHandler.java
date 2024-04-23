package com.jimmy.friday.client.handler;

import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.client.base.Handler;
import com.jimmy.friday.client.support.GatewayInvokeSupport;
import io.netty.channel.ChannelHandlerContext;

public class RpcInvokeHandler implements Handler {

    @Override
    public void handler(Event event, ChannelHandlerContext ctx) {
        GatewayInvokeSupport.notify(event);
    }
}
