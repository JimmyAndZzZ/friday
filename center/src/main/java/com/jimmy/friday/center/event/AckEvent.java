package com.jimmy.friday.center.event;

import com.jimmy.friday.boot.enums.AckTypeEnum;
import org.springframework.context.ApplicationEvent;

public class AckEvent extends ApplicationEvent {

    private String id;

    private AckTypeEnum ackType;

    public AckEvent(Object source) {
        super(source);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AckTypeEnum getAckType() {
        return ackType;
    }

    public void setAckType(AckTypeEnum ackType) {
        this.ackType = ackType;
    }
}
