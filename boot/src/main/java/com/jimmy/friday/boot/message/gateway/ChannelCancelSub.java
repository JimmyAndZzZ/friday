package com.jimmy.friday.boot.message.gateway;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class ChannelCancelSub implements Message {

    private String appId;

    private String channelName;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.CHANNEL_CANCEL_SUB;
    }
}
