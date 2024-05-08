package com.jimmy.friday.center.action.agent;

import com.jimmy.friday.boot.core.agent.Topology;
import com.jimmy.friday.boot.enums.gateway.ApplicationStatusEnum;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.agent.AgentRegister;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.service.HawkEyeTopologyModuleService;
import com.jimmy.friday.center.core.agent.support.AgentSupport;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AgentRegisterAction implements Action<AgentRegister> {

    @Autowired
    private AgentSupport agentSupport;

    @Autowired
    private HawkEyeTopologyModuleService hawkEyeTopologyModuleService;

    @Override
    public void action(AgentRegister registerMessage, ChannelHandlerContext channelHandlerContext) {
        agentSupport.createHeartbeat(registerMessage, channelHandlerContext.channel());

        Topology topology = new Topology();
        topology.setApplication(registerMessage.getName());
        topology.setMachine(registerMessage.getIp());
        topology.setType("application");
        hawkEyeTopologyModuleService.get(topology, ApplicationStatusEnum.OPEN);
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.AGENT_REGISTER;
    }
}
