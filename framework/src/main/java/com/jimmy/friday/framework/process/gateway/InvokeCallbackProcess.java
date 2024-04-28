package com.jimmy.friday.framework.process.gateway;

import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.InvokeCallback;
import com.jimmy.friday.framework.base.Process;
import com.jimmy.friday.framework.support.CallbackSupport;
import com.jimmy.friday.framework.utils.JsonUtil;
import io.netty.channel.ChannelHandlerContext;

import java.util.Objects;

public class InvokeCallbackProcess implements Process {

    private CallbackSupport callbackSupport;

    public InvokeCallbackProcess(CallbackSupport callbackSupport) {
        this.callbackSupport = callbackSupport;
    }

    @Override
    public void process(Event event, ChannelHandlerContext ctx) {
        callbackSupport.callback(Objects.requireNonNull(JsonUtil.parseObject(event.getMessage(), InvokeCallback.class)));
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.INVOKE_CALLBACK;
    }
}
