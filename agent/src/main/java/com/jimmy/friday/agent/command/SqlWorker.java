package com.jimmy.friday.agent.command;

import com.jimmy.friday.agent.other.jdbc.SqlSession;
import com.jimmy.friday.agent.support.TransmitSupport;
import com.jimmy.friday.boot.core.agent.Command;
import com.jimmy.friday.boot.enums.agent.CommandTypeEnum;
import com.jimmy.friday.boot.message.agent.AgentCommand;

import java.util.Collection;

public class SqlWorker extends BaseWorker {

    @Override
    public void open(Command command) {
        Collection<String> list = SqlSession.list();

        StringBuilder sb = new StringBuilder();
        if (list != null && !list.isEmpty()) {
            for (String s : list) {
                sb.append(s).append("\n");
            }
        }

        AgentCommand agentCommand = new AgentCommand();
        agentCommand.setTraceId(command.getTraceId());
        agentCommand.setContent(sb.toString());
        TransmitSupport.getInstance().send(agentCommand);
    }

    @Override
    public CommandTypeEnum command() {
        return CommandTypeEnum.SQL;
    }
}
