package com.jimmy.friday.agent.netty.client;

import com.google.common.base.Strings;
import com.jimmy.friday.agent.base.CommandWorker;
import com.jimmy.friday.agent.core.ConfigLoad;
import com.jimmy.friday.agent.support.CommandSupport;
import com.jimmy.friday.agent.utils.JsonUtil;
import com.jimmy.friday.boot.core.agent.Command;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.agent.AgentHeartbeat;
import com.jimmy.friday.boot.message.agent.AgentRegister;
import com.jimmy.friday.boot.message.agent.AgentCommand;
import com.jimmy.friday.boot.other.ConfigConstants;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class AgentClientHandler extends SimpleChannelInboundHandler<Event> {

    private AgentClient agentClient;

    public AgentClientHandler(AgentClient agentClient) {
        super();
        this.agentClient = agentClient;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String ip = ConfigLoad.getDefault().get(ConfigConstants.ADDRESS);
        String applicationName = ConfigLoad.getDefault().getApplicationName();

        ctx.writeAndFlush(new Event(EventTypeEnum.AGENT_REGISTER, JsonUtil.toString(AgentRegister.build(applicationName, ip))));
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Event event) throws Exception {
        String type = event.getType();
        String message = event.getMessage();

        EventTypeEnum eventTypeEnum = EventTypeEnum.queryByCode(type);
        if (eventTypeEnum == null) {
            return;
        }

        switch (eventTypeEnum) {
            case AGENT_COMMAND:
                AgentCommand agentCommand = JsonUtil.toBean(message, AgentCommand.class);
                if (agentCommand != null) {
                    try {
                        Command command = new Command();
                        command.setParam(agentCommand.getParam());
                        command.setCommand(agentCommand.getCommand());
                        command.setTraceId(agentCommand.getTraceId());

                        CommandWorker commandWorker = CommandSupport.get().getWorker(command.getCommand());
                        commandWorker.open(command);
                    } catch (Exception e) {
                        String errorMessage = e.getMessage();
                        if (Strings.isNullOrEmpty(errorMessage)) {
                            errorMessage = e.toString();
                        }

                        agentCommand.setIsSuccess(false);
                        agentCommand.setErrorMessage(errorMessage);
                        ctx.writeAndFlush(new Event(eventTypeEnum, JsonUtil.toString(agentCommand)));
                    }
                }

                break;
            case AGENT_HEARTBEAT:
                AgentHeartbeat heartbeatMessage = JsonUtil.toBean(message, AgentHeartbeat.class);
                heartbeatMessage.setIp(ConfigLoad.getDefault().get(ConfigConstants.ADDRESS));
                heartbeatMessage.setName(ConfigLoad.getDefault().getApplicationName());
                ctx.writeAndFlush(new Event(EventTypeEnum.AGENT_HEARTBEAT, JsonUtil.toString(heartbeatMessage)));
                break;
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        agentClient.connect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
