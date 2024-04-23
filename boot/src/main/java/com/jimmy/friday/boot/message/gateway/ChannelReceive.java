package com.jimmy.friday.boot.message.gateway;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class ChannelReceive implements Message {

    private Long id;

    private String message;

    private Long offset;

    private String channelName;

    private String appId;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.CHANNEL_RECEIVE;
    }
}
