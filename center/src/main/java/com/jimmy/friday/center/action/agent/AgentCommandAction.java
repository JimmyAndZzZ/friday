package com.jimmy.friday.center.action.agent;

import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.agent.AgentCommand;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.support.CommandSupport;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AgentCommandAction implements Action<AgentCommand> {

    @Autowired
    private CommandSupport commandSupport;

    @Override
    public void action(AgentCommand agentCommand, ChannelHandlerContext channelHandlerContext) {
        commandSupport.notify(agentCommand);
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.AGENT_COMMAND;
    }
}
