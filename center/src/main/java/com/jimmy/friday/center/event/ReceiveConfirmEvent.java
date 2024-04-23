package com.jimmy.friday.center.event;

import com.jimmy.friday.boot.enums.AckTypeEnum;
import org.springframework.context.ApplicationEvent;

public class ReceiveConfirmEvent extends ApplicationEvent {

    private Long id;

    private AckTypeEnum ackType;

    private String errorMessage;

    public ReceiveConfirmEvent(Object source) {
        super(source);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AckTypeEnum getAckType() {
        return ackType;
    }

    public void setAckType(AckTypeEnum ackType) {
        this.ackType = ackType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
