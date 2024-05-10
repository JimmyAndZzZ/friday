package com.jimmy.friday.framework.process.gateway;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.ChannelPushConfirm;
import com.jimmy.friday.framework.base.Process;
import com.jimmy.friday.framework.support.ChannelSupport;
import io.netty.channel.ChannelHandlerContext;

public class ChannelPushConfirmProcess implements Process<ChannelPushConfirm> {

    private ChannelSupport channelSupport;

    public ChannelPushConfirmProcess(ChannelSupport channelSupport) {
        this.channelSupport = channelSupport;
    }

    @Override
    public void process(ChannelPushConfirm channelPushConfirm, ChannelHandlerContext ctx) {
        channelSupport.notify(channelPushConfirm);
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.CHANNEL_PUSH_CONFIRM;
    }
}
