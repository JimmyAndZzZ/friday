package com.jimmy.friday.boot.message.agent;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.core.agent.Topology;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class AgentTopology implements Message {

    private Topology to;

    private Topology from;

    private String invokeRemark;

    private String invokeType;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.AGENT_TOPOLOGY;
    }
}
