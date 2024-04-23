package com.jimmy.friday.boot.message;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class ClientDisconnect implements Message {

    private String id;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.CLIENT_DISCONNECT;
    }
}
