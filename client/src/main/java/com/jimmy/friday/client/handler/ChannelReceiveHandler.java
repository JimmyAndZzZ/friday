package com.jimmy.friday.client.handler;

import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.ChannelAck;
import com.jimmy.friday.boot.message.gateway.ChannelReceive;
import com.jimmy.friday.client.base.Handler;
import com.jimmy.friday.client.support.ChannelSupport;
import com.jimmy.friday.client.utils.JsonUtil;
import io.netty.channel.ChannelHandlerContext;

public class ChannelReceiveHandler implements Handler {

    @Override
    public void handler(Event event, ChannelHandlerContext ctx) {
        String message = event.getMessage();
        ChannelReceive channelReceive = JsonUtil.parseObject(message, ChannelReceive.class);

        ctx.writeAndFlush(new Event(EventTypeEnum.CHANNEL_ACK, JsonUtil.toString(ChannelAck.success(channelReceive.getId()))));
        ChannelSupport.receive(channelReceive);
    }
}
