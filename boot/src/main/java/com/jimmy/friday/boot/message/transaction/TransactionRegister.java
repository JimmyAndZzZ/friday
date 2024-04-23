package com.jimmy.friday.boot.message.transaction;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.core.transaction.TransactionFacts;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class TransactionRegister implements Message {

    private String id;

    private Long traceId;

    private TransactionFacts transactionFacts;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.TRANSACTION_REGISTER;
    }
}
