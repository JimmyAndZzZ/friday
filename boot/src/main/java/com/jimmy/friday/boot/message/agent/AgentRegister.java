package com.jimmy.friday.boot.message.agent;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

@Data
public class AgentRegister implements Message {

    private String name;

    private String ip;

    private Integer heartbeatInterval = 60;

    private Integer heartbeatTimeout = 30;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.AGENT_REGISTER;
    }

    public AgentRegister() {

    }

    public static AgentRegister build(String applicationName, String ip) {
        AgentRegister registerMessage = new AgentRegister();
        registerMessage.setName(applicationName);
        registerMessage.setIp(ip);
        return registerMessage;
    }
}
