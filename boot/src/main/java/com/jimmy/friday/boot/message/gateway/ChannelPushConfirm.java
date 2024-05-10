package com.jimmy.friday.boot.message.gateway;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.ConfirmTypeEnum;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class ChannelPushConfirm implements Message {

    private Long id;

    private ConfirmTypeEnum ackType;

    private String errorMessage;

    public ChannelPushConfirm() {

    }

    public static ChannelPushConfirm success(Long id) {
        ChannelPushConfirm channelPushConfirm = new ChannelPushConfirm();
        channelPushConfirm.setId(id);
        channelPushConfirm.setAckType(ConfirmTypeEnum.SUCCESS);
        return channelPushConfirm;
    }

    public static ChannelPushConfirm fail(Long id, String errorMessage) {
        ChannelPushConfirm channelPushConfirm = new ChannelPushConfirm();
        channelPushConfirm.setId(id);
        channelPushConfirm.setAckType(ConfirmTypeEnum.ERROR);
        channelPushConfirm.setErrorMessage(errorMessage);
        return channelPushConfirm;
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.CHANNEL_PUSH_CONFIRM;
    }
}
