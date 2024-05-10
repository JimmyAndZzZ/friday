package com.jimmy.friday.boot.message.transaction;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.AckTypeEnum;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class TransactionAck implements Message {

    private Long traceId;

    private String transactionId;

    private AckTypeEnum ackTypeEnum;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.TRANSACTION_ACK;
    }
}
