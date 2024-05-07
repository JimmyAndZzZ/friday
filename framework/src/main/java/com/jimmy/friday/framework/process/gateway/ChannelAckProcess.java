package com.jimmy.friday.framework.process.gateway;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.ChannelAck;
import com.jimmy.friday.framework.base.Process;
import com.jimmy.friday.framework.support.ChannelSupport;
import io.netty.channel.ChannelHandlerContext;

public class ChannelAckProcess implements Process<ChannelAck> {

    private ChannelSupport channelSupport;

    public ChannelAckProcess(ChannelSupport channelSupport) {
        this.channelSupport = channelSupport;
    }

    @Override
    public void process(ChannelAck channelAck, ChannelHandlerContext ctx) {
        channelSupport.notify(channelAck);
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.CHANNEL_ACK;
    }
}
