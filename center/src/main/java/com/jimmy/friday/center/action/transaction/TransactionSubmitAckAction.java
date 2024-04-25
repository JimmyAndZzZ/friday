package com.jimmy.friday.center.action.transaction;

import cn.hutool.core.collection.CollUtil;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.transaction.TransactionSubmitAck;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.core.transaction.TransactionManager;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TransactionSubmitAckAction implements Action<TransactionSubmitAck> {

    @Autowired
    private TransactionManager transactionManager;

    @Override
    public void action(TransactionSubmitAck transactionSubmitAck, ChannelHandlerContext channelHandlerContext) {
        List<Long> factIds = transactionSubmitAck.getFactIds();
        String transactionId = transactionSubmitAck.getTransactionId();

        if (CollUtil.isNotEmpty(factIds)) {
            transactionManager.removeTransactionFacts(transactionId, factIds);
        }

        if (CollUtil.isEmpty(transactionManager.getTransactionFacts(transactionId))) {
            transactionManager.removeTransactionPoint(transactionId);
        }
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.TRANSACTION_SUBMIT_ACK;
    }
}
