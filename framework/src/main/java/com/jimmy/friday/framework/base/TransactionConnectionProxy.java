package com.jimmy.friday.framework.base;

import com.jimmy.friday.boot.core.transaction.TransactionFacts;
import com.jimmy.friday.boot.enums.transaction.TransactionTypeEnum;
import com.jimmy.friday.framework.annotation.Transactional;
import com.jimmy.friday.framework.transaction.def.TransactionInfo;

import java.lang.reflect.Method;
import java.sql.Connection;

public interface TransactionConnectionProxy {

    Connection getConnection(Connection connection, String id, String dsName);

    void callback(TransactionFacts transactionFacts);

    TransactionTypeEnum type();

    void preExecute(TransactionInfo info);

    void afterExecute(TransactionInfo info);

    void errorExecute(TransactionInfo info, Throwable throwable);

    void exitExecute(TransactionInfo info);

    TransactionInfo buildTransactionInfo(Transactional transactional, Method method, Class<?> superClass, Object[] objects);
}
