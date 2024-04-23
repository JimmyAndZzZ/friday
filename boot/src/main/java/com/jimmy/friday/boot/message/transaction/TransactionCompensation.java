package com.jimmy.friday.boot.message.transaction;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class TransactionCompensation implements Message {

    private String transactionId;

    private String applicationId;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.TRANSACTION_COMPENSATION;
    }
}
