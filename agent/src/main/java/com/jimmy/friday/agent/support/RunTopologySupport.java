package com.jimmy.friday.agent.support;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.primitives.Ints;
import com.jimmy.friday.agent.core.ConfigLoad;
import com.jimmy.friday.boot.core.agent.RunTopology;
import com.jimmy.friday.boot.core.agent.Topology;
import com.jimmy.friday.boot.message.agent.AgentRunTopology;
import com.jimmy.friday.boot.other.ConfigConstants;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RunTopologySupport {

    private List<RunTopology> runTopologies = Lists.newArrayList();

    private ConcurrentLinkedQueue<RunTopology> cache = Queues.newConcurrentLinkedQueue();

    private static class SingletonHolder {
        private static final RunTopologySupport INSTANCE = new RunTopologySupport();
    }

    private RunTopologySupport() {
        new Thread(() -> {
            String batch = ConfigLoad.getDefault().get(ConfigConstants.BATCH_SIZE);
            int i = Strings.isNullOrEmpty(batch) ? 50 : Ints.tryParse(batch);

            while (true) {
                try {
                    Thread.sleep(100);

                    RunTopology poll = cache.poll();
                    if (poll != null) {
                        runTopologies.add(poll);

                        if (runTopologies.size() >= i) {
                            List<RunTopology> messages = Lists.newArrayList(runTopologies);
                            runTopologies.clear();

                            AgentRunTopology agentRunTopology = new AgentRunTopology();
                            agentRunTopology.setRunTopologyList(messages);
                            TransmitSupport.getInstance().send(agentRunTopology);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static RunTopologySupport getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void send(Topology from,
                     Topology to,
                     String invokeRemark,
                     String invokeType,
                     String traceId) {
        RunTopology runTopology = new RunTopology();
        runTopology.setFrom(from);
        runTopology.setTo(to);
        runTopology.setInvokeRemark(invokeRemark);
        runTopology.setInvokeType(invokeType);
        runTopology.setTraceId(traceId);
        runTopology.setDate(new Date());
        this.cache.offer(runTopology);
    }
}
