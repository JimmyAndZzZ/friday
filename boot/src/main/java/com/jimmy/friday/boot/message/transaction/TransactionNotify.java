package com.jimmy.friday.boot.message.transaction;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.core.transaction.TransactionFacts;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.enums.transaction.TransactionStatusEnum;
import lombok.Data;

import java.util.List;

@Data
public class TransactionNotify implements Message {

    private String id;

    private TransactionStatusEnum transactionStatus;

    private List<TransactionFacts> transactionFacts;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.TRANSACTION_NOTIFY;
    }
}
