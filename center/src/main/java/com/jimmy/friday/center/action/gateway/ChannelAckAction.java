package com.jimmy.friday.center.action.gateway;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.gateway.ChannelAck;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.event.ReceiveConfirmEvent;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChannelAckAction implements Action<ChannelAck>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void action(ChannelAck channelAck, ChannelHandlerContext channelHandlerContext) {
        ReceiveConfirmEvent receiveConfirmEvent = new ReceiveConfirmEvent(applicationContext);
        receiveConfirmEvent.setId(channelAck.getId());
        receiveConfirmEvent.setAckType(channelAck.getAckType());
        receiveConfirmEvent.setErrorMessage(channelAck.getErrorMessage());
        applicationContext.publishEvent(receiveConfirmEvent);
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.CHANNEL_ACK;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
