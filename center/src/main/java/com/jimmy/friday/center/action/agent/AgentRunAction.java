package com.jimmy.friday.center.action.agent;

import cn.hutool.core.collection.CollUtil;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.agent.AgentRunTopology;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.service.HawkEyeLogTopologyRelationService;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AgentRunAction implements Action<AgentRunTopology> {

    @Autowired
    private HawkEyeLogTopologyRelationService hawkEyeLogTopologyRelationService;

    @Override
    public void action(AgentRunTopology agentRunTopology, ChannelHandlerContext channelHandlerContext) {
        if (CollUtil.isEmpty(agentRunTopology.getRunTopologyList())) {
            return;
        }

        hawkEyeLogTopologyRelationService.save(agentRunTopology);
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.AGENT_RUN;
    }
}
