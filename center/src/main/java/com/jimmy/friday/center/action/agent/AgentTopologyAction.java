package com.jimmy.friday.center.action.agent;

import com.jimmy.friday.boot.core.agent.Topology;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.agent.AgentTopology;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.service.HawkEyeTopologyRelationService;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AgentTopologyAction implements Action<AgentTopology> {

    @Autowired
    private HawkEyeTopologyRelationService hawkEyeTopologyRelationService;

    @Override
    public void action(AgentTopology agentTopology, ChannelHandlerContext channelHandlerContext) {
        Topology from = agentTopology.getFrom();
        Topology to = agentTopology.getTo();
        hawkEyeTopologyRelationService.add(from, to, agentTopology.getInvokeRemark(), agentTopology.getInvokeType());
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.AGENT_TOPOLOGY;
    }
}
