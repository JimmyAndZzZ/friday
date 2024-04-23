package com.jimmy.friday.boot.message.agent;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.core.agent.Trace;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

import java.util.List;

@Data
public class AgentLog implements Message {

    private List<Trace> traceList;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.AGENT_LOG;
    }
}
