package com.jimmy.friday.framework.process.transaction;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.transaction.TransactionRefund;
import com.jimmy.friday.framework.base.Process;
import com.jimmy.friday.framework.transaction.TransactionSession;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionRefundProcess implements Process<TransactionRefund> {

    private TransactionSession transactionSession;

    public TransactionRefundProcess(TransactionSession transactionSession) {
        this.transactionSession = transactionSession;
    }

    @Override
    public void process(TransactionRefund transactionRefund, ChannelHandlerContext ctx) {
        log.info("收到事务提交退回:{}", transactionRefund);
        transactionSession.callback(transactionRefund.getTransactionFacts());
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.TRANSACTION_REFUND;
    }
}
