package com.jimmy.friday.boot.message.agent;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.core.agent.RunTopology;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

import java.util.List;

@Data
public class AgentRunTopology implements Message {

    private List<RunTopology> runTopologyList;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.AGENT_RUN;
    }
}
