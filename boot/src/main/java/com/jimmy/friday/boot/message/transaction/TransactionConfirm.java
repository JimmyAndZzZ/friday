package com.jimmy.friday.boot.message.transaction;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.ConfirmTypeEnum;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class TransactionConfirm implements Message {

    private Long traceId;

    private String transactionId;

    private ConfirmTypeEnum confirmTypeEnum;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.TRANSACTION_CONFIRM;
    }
}
