package com.jimmy.friday.framework.process;

import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.ChannelAck;
import com.jimmy.friday.framework.base.Process;
import com.jimmy.friday.framework.support.ChannelSupport;
import com.jimmy.friday.framework.utils.JsonUtil;
import io.netty.channel.ChannelHandlerContext;

public class ChannelAckProcess implements Process {

    private ChannelSupport channelSupport;

    public ChannelAckProcess(ChannelSupport channelSupport) {
        this.channelSupport = channelSupport;
    }

    @Override
    public void process(Event event, ChannelHandlerContext ctx) {
        String message = event.getMessage();
        ChannelAck channelAck = JsonUtil.parseObject(message, ChannelAck.class);

        channelSupport.notify(channelAck);
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.CHANNEL_ACK;
    }
}
