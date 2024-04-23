package com.jimmy.friday.agent.support;

import com.jimmy.friday.boot.core.agent.Topology;
import com.jimmy.friday.boot.message.agent.AgentTopology;

import java.util.concurrent.ConcurrentHashMap;

public class TopologySupport {

    private final ConcurrentHashMap<String, Boolean> push = new ConcurrentHashMap<>();

    private static class SingletonHolder {
        private static final TopologySupport INSTANCE = new TopologySupport();
    }

    private TopologySupport() {
    }

    public static TopologySupport getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void push(Topology from, Topology to, String invokeRemark, String invokeType) {
        AgentTopology agentTopology = new AgentTopology();
        agentTopology.setFrom(from);
        agentTopology.setTo(to);
        agentTopology.setInvokeRemark(invokeRemark);
        agentTopology.setInvokeType(invokeType);

        if (push.putIfAbsent(agentTopology.toString(), true) == null) {
            TransmitSupport.getInstance().send(agentTopology);
        }
    }
}