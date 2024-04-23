package com.jimmy.friday.framework.process;

import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.transaction.TransactionNotify;
import com.jimmy.friday.framework.base.Process;
import com.jimmy.friday.framework.transaction.TransactionSession;
import com.jimmy.friday.framework.utils.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionNotifyProcess implements Process {

    private TransactionSession transactionSession;

    public TransactionNotifyProcess(TransactionSession transactionSession) {
        this.transactionSession = transactionSession;
    }

    @Override
    public void process(Event event, ChannelHandlerContext ctx) {
        String message = event.getMessage();

        log.info("收到事务提交回调:{}", message);

        TransactionNotify transactionNotify = JsonUtil.parseObject(message, TransactionNotify.class);
        transactionSession.submitNotify(transactionNotify.getId(), transactionNotify.getTransactionStatus(), transactionNotify.getTransactionFacts());
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.TRANSACTION_NOTIFY;
    }
}
