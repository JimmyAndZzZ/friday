package com.jimmy.friday.boot.message.transaction;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

import java.util.List;

@Data
public class TransactionSubmitAck implements Message {

    private List<Long> factIds;

    private String transactionId;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.TRANSACTION_SUBMIT_ACK;
    }
}
