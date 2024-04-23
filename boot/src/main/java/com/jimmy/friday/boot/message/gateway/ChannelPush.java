package com.jimmy.friday.boot.message.gateway;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class ChannelPush implements Message {

    private Long id;

    private String message;

    private String serviceName;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.CHANNEL_PUSH;
    }
}
