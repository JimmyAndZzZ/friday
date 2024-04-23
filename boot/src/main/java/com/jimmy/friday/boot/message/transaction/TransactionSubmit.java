package com.jimmy.friday.boot.message.transaction;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.enums.TransactionStatusEnum;
import lombok.Data;

@Data
public class TransactionSubmit implements Message {

    private String id;

    private Long traceId;

    private TransactionStatusEnum transactionStatus;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.TRANSACTION_SUBMIT;
    }
}
