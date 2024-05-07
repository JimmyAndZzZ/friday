package com.jimmy.friday.framework.process.transaction;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.transaction.TransactionNotify;
import com.jimmy.friday.framework.base.Process;
import com.jimmy.friday.framework.transaction.TransactionSession;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionNotifyProcess implements Process<TransactionNotify> {

    private TransactionSession transactionSession;

    public TransactionNotifyProcess(TransactionSession transactionSession) {
        this.transactionSession = transactionSession;
    }

    @Override
    public void process(TransactionNotify transactionNotify, ChannelHandlerContext ctx) {
        log.info("收到事务提交回调:{}", transactionNotify);
        transactionSession.submitNotify(transactionNotify.getId(), transactionNotify.getTransactionStatus(), transactionNotify.getTransactionFacts());
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.TRANSACTION_NOTIFY;
    }
}
