package com.jimmy.friday.boot.message;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.AckTypeEnum;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class Ack implements Message {

    private String id;

    private AckTypeEnum ackType;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.ACK;
    }
}
