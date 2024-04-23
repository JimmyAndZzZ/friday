package com.jimmy.friday.boot.message.agent;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class AgentHeartbeat implements Message {

    private Long traceId;

    private String name;

    private String ip;

    public AgentHeartbeat() {

    }

    public AgentHeartbeat(Long traceId) {
        this.traceId = traceId;
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.AGENT_HEARTBEAT;
    }
}
