package com.jimmy.friday.framework.process;

import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.framework.base.Process;
import io.netty.channel.ChannelHandlerContext;

public class AckProcess implements Process {

    @Override
    public void process(Event event, ChannelHandlerContext ctx) {

    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.ACK;
    }
}
