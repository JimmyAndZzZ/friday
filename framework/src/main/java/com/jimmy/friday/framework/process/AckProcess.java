package com.jimmy.friday.framework.process;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.Ack;
import com.jimmy.friday.framework.base.Process;
import com.jimmy.friday.framework.other.AckEvent;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.context.ApplicationContext;

public class AckProcess implements Process<Ack> {

    private ApplicationContext applicationContext;

    public AckProcess(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void process(Ack ack, ChannelHandlerContext ctx) {
        AckEvent ackEvent = new AckEvent(applicationContext);
        ackEvent.setId(ack.getId());
        applicationContext.publishEvent(ackEvent);
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.ACK;
    }
}
