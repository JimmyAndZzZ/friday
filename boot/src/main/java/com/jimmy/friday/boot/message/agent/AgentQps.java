package com.jimmy.friday.boot.message.agent;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.core.agent.Qps;
import com.jimmy.friday.boot.core.agent.Topology;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AgentQps implements Message {

    private Topology server;

    private List<Qps> qpsList = new ArrayList<>();

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.AGENT_QPS;
    }
}
