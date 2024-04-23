package com.jimmy.friday.boot.message.agent;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class AgentShutdown implements Message {

    private String name;

    private String ip;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.AGENT_HEARTBEAT;
    }
}
