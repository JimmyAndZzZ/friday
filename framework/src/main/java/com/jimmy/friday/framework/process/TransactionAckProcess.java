package com.jimmy.friday.framework.process;

import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.transaction.TransactionAck;
import com.jimmy.friday.framework.base.Process;
import com.jimmy.friday.framework.transaction.TransactionSession;
import com.jimmy.friday.framework.utils.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionAckProcess implements Process {

    private TransactionSession transactionSession;

    public TransactionAckProcess(TransactionSession transactionSession) {
        this.transactionSession = transactionSession;
    }

    @Override
    public void process(Event event, ChannelHandlerContext ctx) {
        String message = event.getMessage();

        TransactionAck transactionSubmitAck = JsonUtil.parseObject(message, TransactionAck.class);
        transactionSession.notify(transactionSubmitAck.getTraceId(), transactionSubmitAck.getAckTypeEnum());
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.TRANSACTION_ACK;
    }
}
