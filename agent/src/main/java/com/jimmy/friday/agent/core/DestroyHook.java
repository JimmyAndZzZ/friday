package com.jimmy.friday.agent.core;

import com.jimmy.friday.agent.support.TransmitSupport;
import com.jimmy.friday.boot.message.agent.AgentShutdown;
import com.jimmy.friday.boot.other.ConfigConstants;

public class DestroyHook {

    public static void shutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            String ip = ConfigLoad.getDefault().get(ConfigConstants.ADDRESS);
            String applicationName = ConfigLoad.getDefault().getApplicationName();

            AgentShutdown agentShutdown = new AgentShutdown();
            agentShutdown.setName(applicationName);
            agentShutdown.setIp(ip);
            TransmitSupport.getInstance().broadcast(agentShutdown);
        }));
    }
}
