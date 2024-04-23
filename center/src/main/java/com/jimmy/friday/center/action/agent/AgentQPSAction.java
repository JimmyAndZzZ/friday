package com.jimmy.friday.center.action.agent;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.agent.AgentQps;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.service.HawkEyeQpsService;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AgentQPSAction implements Action<AgentQps> {

    @Autowired
    private HawkEyeQpsService hawkEyeQpsService;

    @Override
    public void action(AgentQps agentQps, ChannelHandlerContext channelHandlerContext) {
        hawkEyeQpsService.save(agentQps);
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.AGENT_QPS;
    }
}
