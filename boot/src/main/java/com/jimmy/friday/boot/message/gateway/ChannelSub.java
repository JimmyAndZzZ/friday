package com.jimmy.friday.boot.message.gateway;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class ChannelSub implements Message {

    private String appId;

    private String channelName;

    private String applicationId;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.CHANNEL_SUB;
    }
}
