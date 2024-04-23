package com.jimmy.friday.client.base;

import com.jimmy.friday.boot.core.Event;
import io.netty.channel.ChannelHandlerContext;

public interface Handler {

    void handler(Event event, ChannelHandlerContext ctx);
}
