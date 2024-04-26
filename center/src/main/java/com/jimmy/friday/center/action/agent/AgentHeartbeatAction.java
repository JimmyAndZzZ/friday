package com.jimmy.friday.center.action.agent;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.agent.AgentHeartbeat;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.core.agent.support.AgentSupport;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AgentHeartbeatAction implements Action<AgentHeartbeat> {

    @Autowired
    private AgentSupport agentSupport;

    @Override
    public void action(AgentHeartbeat heartbeatMessage, ChannelHandlerContext channelHandlerContext) {
        log.info("收到agent心跳,name:{},ip:{}", heartbeatMessage.getName(), heartbeatMessage.getIp());
        agentSupport.release(heartbeatMessage);
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.AGENT_HEARTBEAT;
    }
}
