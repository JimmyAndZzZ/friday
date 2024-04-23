package com.jimmy.friday.center.base;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import io.netty.channel.ChannelHandlerContext;

public interface Action<T> {

    void action(T t, ChannelHandlerContext channelHandlerContext);

    EventTypeEnum type();
}
