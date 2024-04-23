package com.jimmy.friday.center.action.agent;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.agent.AgentShutdown;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.support.AgentSupport;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AgentShutdownAction implements Action<AgentShutdown> {

    @Autowired
    private AgentSupport agentSupport;

    @Override
    public void action(AgentShutdown agentShutdown, ChannelHandlerContext channelHandlerContext) {
        log.info("agent下线,name:{},ip:{}", agentShutdown.getName(), agentShutdown.getIp());
        agentSupport.remove(agentShutdown.getName(),agentShutdown.getIp());
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.AGENT_SHUTDOWN;
    }
}
