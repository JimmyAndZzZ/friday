package com.jimmy.friday.agent.support;

import com.jimmy.friday.agent.core.ConfigLoad;
import com.jimmy.friday.agent.netty.client.AgentClient;
import com.jimmy.friday.agent.utils.StringUtil;
import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.other.ConfigConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransmitSupport {

    private AgentClient master;

    private final List<AgentClient> backup = new ArrayList<>();

    private static class SingletonHolder {
        private static final TransmitSupport INSTANCE = new TransmitSupport();
    }

    private TransmitSupport() {
        String server = ConfigLoad.getDefault().get(ConfigConstants.COLLECTOR_SERVER);
        if (StringUtil.isEmpty(server)) {
            throw new GatewayException("未配置服务端地址或端口");
        }

        Set<String> repeat = new HashSet<>();

        String masterServer = server;
        if (StringUtil.contains(server, "backup")) {
            masterServer = StringUtil.subBefore(server, "?backup=", false);

            String s = StringUtil.subAfter(server, "?backup=", false);
            if (StringUtil.isNotEmpty(s)) {
                String[] split = s.split(",");

                for (String string : split) {
                    string = string.trim();
                    if (!string.equalsIgnoreCase(masterServer) && repeat.add(string)) {
                        AgentClient agentClient = new AgentClient(string);
                        agentClient.init();
                        this.backup.add(agentClient);
                    }
                }
            }
        }

        this.master = new AgentClient(masterServer);
        this.master.init();
    }

    public static TransmitSupport getInstance() {
        return TransmitSupport.SingletonHolder.INSTANCE;
    }

    public void send(Message message) {
        if (master.getConnectSuccess()) {
            master.send(message);
            return;
        }

        if (!backup.isEmpty()) {
            for (AgentClient agentClient : backup) {
                if (agentClient.getConnectSuccess()) {
                    agentClient.send(message);
                    return;
                }
            }
        }
    }

    public void broadcast(Message message) {
        if (master.getConnectSuccess()) {
            master.send(message);
        }

        if (!backup.isEmpty()) {
            for (AgentClient agentClient : backup) {
                if (agentClient.getConnectSuccess()) {
                    agentClient.send(message);
                }
            }
        }
    }
}
