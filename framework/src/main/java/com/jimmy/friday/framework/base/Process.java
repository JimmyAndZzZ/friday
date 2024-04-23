package com.jimmy.friday.framework.base;

import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import io.netty.channel.ChannelHandlerContext;

public interface Process {

    void process(Event event, ChannelHandlerContext ctx);

    EventTypeEnum type();
}
