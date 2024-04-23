package com.jimmy.friday.boot.message.gateway;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.AckTypeEnum;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class ChannelAck implements Message {

    private Long id;

    private AckTypeEnum ackType;

    private String errorMessage;

    public ChannelAck() {

    }

    public static ChannelAck success(Long id) {
        ChannelAck channelAck = new ChannelAck();
        channelAck.setId(id);
        channelAck.setAckType(AckTypeEnum.SUCCESS);
        return channelAck;
    }

    public static ChannelAck fail(Long id, String errorMessage) {
        ChannelAck channelAck = new ChannelAck();
        channelAck.setId(id);
        channelAck.setAckType(AckTypeEnum.ERROR);
        channelAck.setErrorMessage(errorMessage);
        return channelAck;
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.CHANNEL_ACK;
    }
}
