package com.jimmy.friday.framework.config;

import com.jimmy.friday.framework.callback.TransactionCallback;
import com.jimmy.friday.framework.bootstrap.TransactionBootstrap;
import com.jimmy.friday.framework.core.ConfigLoad;
import com.jimmy.friday.framework.core.GlobalCache;
import com.jimmy.friday.framework.process.TransactionAckProcess;
import com.jimmy.friday.framework.process.TransactionNotifyProcess;
import com.jimmy.friday.framework.process.TransactionRefundProcess;
import com.jimmy.friday.framework.proxy.LcnTransactionProxy;
import com.jimmy.friday.framework.proxy.TccTransactionProxy;
import com.jimmy.friday.framework.support.TransmitSupport;
import com.jimmy.friday.framework.transaction.TransactionSession;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransactionConfig {

    @Bean
    public TransactionSession transactionSession(ConfigLoad configLoad, TransmitSupport transmitSupport, ApplicationContext applicationContext) {
        return new TransactionSession(configLoad, transmitSupport, applicationContext);
    }

    @Bean
    public TransactionCallback transactionCallback(TransactionSession transactionSession) {
        return new TransactionCallback(transactionSession);
    }

    @Bean
    public TransactionBootstrap transactionBootstrap(ConfigLoad configLoad, TransactionSession transactionSession) {
        return new TransactionBootstrap(configLoad, transactionSession);
    }

    @Configuration
    protected static class ProxyConfig {

        @Bean
        public LcnTransactionProxy lcnTransactionProxy(GlobalCache globalCache, TransactionSession transactionSession, TransmitSupport transmitSupport) {
            return new LcnTransactionProxy(globalCache, transactionSession, transmitSupport);
        }

        @Bean
        public TccTransactionProxy tccTransactionProxy(GlobalCache globalCache, TransactionSession transactionSession, TransmitSupport transmitSupport) {
            return new TccTransactionProxy(globalCache, transactionSession, transmitSupport);
        }
    }

    @Configuration
    protected static class ProcessConfig {

        @Bean
        public TransactionNotifyProcess transactionNotifyProcess(TransactionSession transactionSession) {
            return new TransactionNotifyProcess(transactionSession);
        }

        @Bean
        public TransactionAckProcess transactionAckProcess(TransactionSession transactionSession) {
            return new TransactionAckProcess(transactionSession);
        }

        @Bean
        public TransactionRefundProcess transactionRefundProcess(TransactionSession transactionSession) {
            return new TransactionRefundProcess(transactionSession);
        }
    }
}
