package com.jimmy.friday.framework.callback;

import com.jimmy.friday.framework.base.Callback;
import com.jimmy.friday.framework.transaction.TransactionSession;
import io.netty.channel.ChannelHandlerContext;

public class TransactionCallback implements Callback {

    private TransactionSession transactionSession;

    public TransactionCallback(TransactionSession transactionSession) {
        this.transactionSession = transactionSession;
    }

    @Override
    public void prepare(ChannelHandlerContext ctx) {
        transactionSession.transactionSubmitRetry();
    }

    @Override
    public void close() {
        transactionSession.close();
    }
}
