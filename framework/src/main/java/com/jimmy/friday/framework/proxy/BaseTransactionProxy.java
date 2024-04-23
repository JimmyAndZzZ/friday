package com.jimmy.friday.framework.proxy;

import com.jimmy.friday.boot.enums.TransactionStatusEnum;
import com.jimmy.friday.boot.enums.TransactionTypeEnum;
import com.jimmy.friday.boot.message.transaction.TransactionTimeout;
import com.jimmy.friday.framework.annotation.Transactional;
import com.jimmy.friday.framework.base.TransactionConnectionProxy;
import com.jimmy.friday.framework.core.GlobalCache;
import com.jimmy.friday.framework.other.DelayConsole;
import com.jimmy.friday.framework.support.TransmitSupport;
import com.jimmy.friday.framework.transaction.TransactionContext;
import com.jimmy.friday.framework.transaction.def.TransactionInfo;
import com.jimmy.friday.framework.transaction.TransactionSession;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public abstract class BaseTransactionProxy implements TransactionConnectionProxy {

    protected GlobalCache globalCache;

    protected DelayConsole delayConsole;

    protected TransmitSupport transmitSupport;

    protected TransactionSession transactionSession;

    public BaseTransactionProxy(GlobalCache globalCache, TransactionSession transactionSession, TransmitSupport transmitSupport) {
        this.globalCache = globalCache;
        this.transmitSupport = transmitSupport;
        this.delayConsole = new DelayConsole();
        this.transactionSession = transactionSession;
    }

    @Override
    public TransactionInfo buildTransactionInfo(Transactional transactional, Method method, Class<?> superClass, Object[] objects) {
        TransactionInfo info = new TransactionInfo();
        info.setCancelMethod(transactional.cancelMethod());
        info.setConfirmMethod(transactional.confirmMethod());
        info.setTransactionTypeEnum(transactional.type());
        info.setTimeout(transactional.timeout());
        info.setIsStart(TransactionContext.isNull());
        return info;
    }

    @Override
    public void preExecute(TransactionInfo info) {
        Boolean isStart = info.getIsStart();
        Integer timeout = info.getTimeout();
        TransactionTypeEnum transactionTypeEnum = info.getTransactionTypeEnum();

        TransactionContext transactionContext = isStart ? TransactionContext.create(transactionTypeEnum) : TransactionContext.setTransactionType(transactionTypeEnum);
        info.setId(transactionContext.getId());

        if (timeout > 0) {
            //超时回调，防止服务端假死未回调(多加30s，防止并发)
            delayConsole.add(transactionContext.getId(), () -> transactionSession.rollback(transactionContext.getId()), timeout + 30, TimeUnit.SECONDS);

            TransactionTimeout transactionTimeout = new TransactionTimeout();
            transactionTimeout.setId(info.getId());
            transactionTimeout.setTimeout(timeout);
            transactionTimeout.setCurrentTimestamp(System.currentTimeMillis());
            transmitSupport.send(transactionTimeout);
        }
    }

    @Override
    public void afterExecute(TransactionInfo info) {
        if (info.getIsStart()) {
            this.transactionSession.submit(info.getId(), TransactionStatusEnum.SUCCESS);
        }
    }

    @Override
    public void errorExecute(TransactionInfo info, Throwable throwable) {
        this.transactionSession.submit(info.getId(), TransactionStatusEnum.FAIL);
        TransactionContext.clear();
    }

    @Override
    public void exitExecute(TransactionInfo info) {
        if (info.getIsStart()) {
            TransactionContext.clear();
        }
    }
}
