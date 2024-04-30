package com.jimmy.friday.center.action;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.Ack;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.core.schedule.ScheduleSession;
import com.jimmy.friday.center.event.AckEvent;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AckAction implements Action<Ack>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void action(Ack ack, ChannelHandlerContext channelHandlerContext) {
        AckEvent ackEvent = new AckEvent(applicationContext);
        ackEvent.setId(ack.getId());
        ackEvent.setAckType(ack.getAckType());
        applicationContext.publishEvent(ackEvent);
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.ACK;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
