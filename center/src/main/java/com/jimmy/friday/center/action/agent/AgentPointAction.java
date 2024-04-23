package com.jimmy.friday.center.action.agent;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.agent.AgentRunPoint;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.service.HawkEyeLogPointService;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AgentPointAction implements Action<AgentRunPoint> {

    @Autowired
    private HawkEyeLogPointService hawkEyeLogPointService;

    @Override
    public void action(AgentRunPoint agentRunPoint, ChannelHandlerContext channelHandlerContext) {
        hawkEyeLogPointService.createPoint(agentRunPoint);
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.AGENT_POINT;
    }
}
