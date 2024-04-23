package com.jimmy.friday.client.handler;

import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.message.gateway.InvokeCallback;
import com.jimmy.friday.client.base.Handler;
import com.jimmy.friday.client.support.CallbackSupport;
import com.jimmy.friday.client.utils.JsonUtil;
import io.netty.channel.ChannelHandlerContext;

import java.util.Objects;

public class InvokeCallbackHandler implements Handler {

    @Override
    public void handler(Event event, ChannelHandlerContext ctx) {
        CallbackSupport.callback(Objects.requireNonNull(JsonUtil.parseObject(event.getMessage(), InvokeCallback.class)));
    }
}
