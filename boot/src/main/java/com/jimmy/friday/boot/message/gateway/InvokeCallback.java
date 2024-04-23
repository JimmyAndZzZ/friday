package com.jimmy.friday.boot.message.gateway;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.enums.NotifyTypeEnum;
import lombok.Data;

@Data
public class InvokeCallback implements Message {

    private Long traceId;

    private String errorMessage;

    private NotifyTypeEnum notifyType;

    private Integer progressRate;

    private Object response;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.INVOKE_CALLBACK;
    }
}
