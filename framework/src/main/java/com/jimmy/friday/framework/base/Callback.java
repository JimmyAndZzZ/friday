package com.jimmy.friday.framework.base;

import io.netty.channel.ChannelHandlerContext;

public interface Callback {

    void prepare(ChannelHandlerContext ctx);

    void close();
}
