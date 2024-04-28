package com.jimmy.friday.framework.process.transaction;

import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.transaction.TransactionRefund;
import com.jimmy.friday.framework.base.Process;
import com.jimmy.friday.framework.transaction.TransactionSession;
import com.jimmy.friday.framework.utils.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionRefundProcess implements Process {

    private TransactionSession transactionSession;

    public TransactionRefundProcess(TransactionSession transactionSession) {
        this.transactionSession = transactionSession;
    }

    @Override
    public void process(Event event, ChannelHandlerContext ctx) {
        String message = event.getMessage();

        log.info("收到事务提交退回:{}", message);

        TransactionRefund transactionRefund = JsonUtil.parseObject(message, TransactionRefund.class);
        transactionSession.callback(transactionRefund.getTransactionFacts());
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.TRANSACTION_REFUND;
    }
}
