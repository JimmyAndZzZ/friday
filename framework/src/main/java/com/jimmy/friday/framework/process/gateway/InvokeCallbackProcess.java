package com.jimmy.friday.framework.process.gateway;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.InvokeCallback;
import com.jimmy.friday.framework.base.Process;
import com.jimmy.friday.framework.support.CallbackSupport;
import io.netty.channel.ChannelHandlerContext;

public class InvokeCallbackProcess implements Process<InvokeCallback> {

    private CallbackSupport callbackSupport;

    public InvokeCallbackProcess(CallbackSupport callbackSupport) {
        this.callbackSupport = callbackSupport;
    }

    @Override
    public void process(InvokeCallback invokeCallback, ChannelHandlerContext ctx) {
        callbackSupport.callback(invokeCallback);
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.INVOKE_CALLBACK;
    }
}
