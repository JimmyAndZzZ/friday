package com.jimmy.friday.framework.proxy;

import com.jimmy.friday.boot.core.transaction.TransactionFacts;
import com.jimmy.friday.boot.enums.TransactionStatusEnum;
import com.jimmy.friday.boot.enums.TransactionTypeEnum;
import com.jimmy.friday.boot.exception.TransactionException;
import com.jimmy.friday.framework.connection.LcnConnection;
import com.jimmy.friday.framework.core.GlobalCache;
import com.jimmy.friday.framework.support.TransmitSupport;
import com.jimmy.friday.framework.transaction.TransactionSession;
import com.jimmy.friday.framework.utils.CacheConstants;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class LcnTransactionProxy extends BaseTransactionProxy {

    public LcnTransactionProxy(GlobalCache globalCache, TransactionSession transactionSession, TransmitSupport transmitSupport) {
        super(globalCache, transactionSession, transmitSupport);
    }

    @Override
    public Connection getConnection(Connection connection, String id, String dsName) {
        LcnConnection lcnConnection = new LcnConnection(connection);

        LcnConnection put = globalCache.putIfAbsent(CacheConstants.TRANSACTION_LCN_CONNECT + id, this.type().name() + ":" + dsName, lcnConnection);
        if (put != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error("连接关闭失败", e);
                throw new TransactionException(e);
            }

            return put;
        }

        TransactionFacts transactionFacts = new TransactionFacts();
        transactionFacts.setDsName(dsName);
        transactionFacts.setTransactionId(id);
        transactionFacts.setTransactionType(this.type());
        transactionSession.collectTransaction(transactionFacts);

        return lcnConnection;
    }

    @Override
    public void callback(TransactionFacts transactionFacts) {
        String transactionId = transactionFacts.getTransactionId();
        String dsName = transactionFacts.getDsName();
        TransactionStatusEnum transactionStatusEnum = transactionFacts.getTransactionStatus();

        LcnConnection lcnConnection = globalCache.get(CacheConstants.TRANSACTION_LCN_CONNECT + transactionId, this.type().name() + ":" + dsName, LcnConnection.class);
        if (lcnConnection == null) {
            log.error("当前lcn上下文不存在,id:{}", transactionId);
            return;
        }

        try {
            lcnConnection.notify(transactionStatusEnum, transactionId);
        } catch (SQLException e) {
            log.error("回调失败，事务id:{}", transactionId, e);
        }
    }

    @Override
    public TransactionTypeEnum type() {
        return TransactionTypeEnum.LCN;
    }
}
