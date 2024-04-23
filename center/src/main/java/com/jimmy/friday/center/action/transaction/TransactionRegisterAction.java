package com.jimmy.friday.center.action.transaction;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.transaction.TransactionRegister;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.core.TransactionManager;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionRegisterAction implements Action<TransactionRegister> {

    @Autowired
    private TransactionManager transactionManager;

    @Override
    public void action(TransactionRegister transactionRegister, ChannelHandlerContext channelHandlerContext) {
        transactionManager.register(transactionRegister.getTransactionFacts(), transactionRegister.getTraceId());
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.TRANSACTION_REGISTER;
    }
}
