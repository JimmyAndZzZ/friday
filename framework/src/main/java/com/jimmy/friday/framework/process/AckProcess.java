package com.jimmy.friday.framework.process;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.Ack;
import com.jimmy.friday.framework.base.Process;
import io.netty.channel.ChannelHandlerContext;

public class AckProcess implements Process<Ack> {

    @Override
    public void process(Ack ack, ChannelHandlerContext ctx) {

    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.ACK;
    }
}
