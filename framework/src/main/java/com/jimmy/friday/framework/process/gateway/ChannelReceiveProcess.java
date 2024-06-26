package com.jimmy.friday.framework.process.gateway;

import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.ChannelPushConfirm;
import com.jimmy.friday.boot.message.gateway.ChannelReceive;
import com.jimmy.friday.framework.base.Process;
import com.jimmy.friday.framework.support.ChannelSupport;
import com.jimmy.friday.framework.utils.JsonUtil;
import io.netty.channel.ChannelHandlerContext;

public class ChannelReceiveProcess implements Process<ChannelReceive> {

    private ChannelSupport channelSupport;

    public ChannelReceiveProcess(ChannelSupport channelSupport) {
        this.channelSupport = channelSupport;
    }

    @Override
    public void process(ChannelReceive channelReceive, ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new Event(EventTypeEnum.CHANNEL_PUSH_CONFIRM, JsonUtil.toString(ChannelPushConfirm.success(channelReceive.getId()))));
        channelSupport.receive(channelReceive);
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.CHANNEL_RECEIVE;
    }
}
