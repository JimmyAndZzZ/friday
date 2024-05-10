package com.jimmy.friday.center.action.gateway;

import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.ChannelPushConfirm;
import com.jimmy.friday.boot.message.gateway.ChannelPush;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.core.gateway.support.ChannelSupport;
import com.jimmy.friday.center.utils.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChannelPushAction implements Action<ChannelPush> {

    @Autowired
    private ChannelSupport channelSupport;

    @Override
    public void action(ChannelPush channelPush, ChannelHandlerContext channelHandlerContext) {
        Long id = channelPush.getId();
        String message = channelPush.getMessage();
        String serviceName = channelPush.getServiceName();

        try {
            //消息推送
            channelSupport.push(message, serviceName);
            //ack
            channelHandlerContext.write(new Event(EventTypeEnum.CHANNEL_PUSH_CONFIRM, JsonUtil.toString(ChannelPushConfirm.success(id))));
        } catch (Exception e) {
            log.error("消息推送失败", e);
            channelHandlerContext.write(new Event(EventTypeEnum.CHANNEL_PUSH_CONFIRM, JsonUtil.toString(ChannelPushConfirm.fail(id, "消息推送失败"))));
        }
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.CHANNEL_PUSH;
    }
}
