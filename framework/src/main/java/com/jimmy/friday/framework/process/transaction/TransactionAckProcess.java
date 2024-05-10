package com.jimmy.friday.framework.process.transaction;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.transaction.TransactionConfirm;
import com.jimmy.friday.framework.base.Process;
import com.jimmy.friday.framework.transaction.TransactionSession;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionAckProcess implements Process<TransactionConfirm> {

    private TransactionSession transactionSession;

    public TransactionAckProcess(TransactionSession transactionSession) {
        this.transactionSession = transactionSession;
    }

    @Override
    public void process(TransactionConfirm transactionSubmitAck, ChannelHandlerContext ctx) {
        transactionSession.notify(transactionSubmitAck.getTraceId(), transactionSubmitAck.getConfirmTypeEnum());
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.TRANSACTION_CONFIRM;
    }
}
