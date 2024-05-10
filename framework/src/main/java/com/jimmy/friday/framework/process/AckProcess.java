package com.jimmy.friday.framework.process;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.Ack;
import com.jimmy.friday.framework.base.Process;
import com.jimmy.friday.framework.support.TransmitSupport;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AckProcess implements Process<Ack> {

    private TransmitSupport transmitSupport;

    public AckProcess(TransmitSupport transmitSupport) {
        this.transmitSupport = transmitSupport;
    }

    @Override
    public void process(Ack ack, ChannelHandlerContext ctx) {
        transmitSupport.notify(ack.getId());
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.ACK;
    }
}
