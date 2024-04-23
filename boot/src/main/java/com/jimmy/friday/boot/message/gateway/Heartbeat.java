package com.jimmy.friday.boot.message.gateway;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class Heartbeat implements Message {

    private Long traceId;

    private Boolean isBusy = false;

    public Heartbeat() {

    }

    public Heartbeat(Long traceId) {
        this.traceId = traceId;
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.HEARTBEAT;
    }
}
