package com.jimmy.friday.boot.message.transaction;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class TransactionTimeout implements Message {

    private String id;

    private Integer timeout;

    private Long currentTimestamp;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.TRANSACTION_TIMEOUT;
    }
}
