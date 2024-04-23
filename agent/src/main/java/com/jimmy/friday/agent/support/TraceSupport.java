package com.jimmy.friday.agent.support;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.primitives.Ints;
import com.jimmy.friday.agent.core.ConfigLoad;
import com.jimmy.friday.agent.memory.MemoryPool;
import com.jimmy.friday.agent.utils.JsonUtil;
import com.jimmy.friday.boot.core.agent.Trace;
import com.jimmy.friday.boot.message.agent.AgentLog;
import com.jimmy.friday.boot.other.ConfigConstants;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class TraceSupport {

    private static TraceSupport traceSupport;

    private List<Index> indices = Lists.newArrayList();

    private ConcurrentLinkedQueue<Index> cache = Queues.newConcurrentLinkedQueue();

    public static void build() {
        traceSupport = new TraceSupport();
    }

    private TraceSupport() {
        new Thread(() -> {
            String batch = ConfigLoad.getDefault().get(ConfigConstants.BATCH_SIZE);
            int i = Strings.isNullOrEmpty(batch) ? 50 : Ints.tryParse(batch);

            while (true) {
                try {
                    Thread.sleep(100);

                    Index poll = cache.poll();
                    if (poll != null) {
                        indices.add(poll);

                        if (indices.size() >= i) {
                            List<Trace> messages = indices.stream().map(index -> JsonUtil.toBean(MemoryPool.get().getString(index.getIndices()), Trace.class)).collect(Collectors.toList());
                            indices.clear();

                            AgentLog agentLog = new AgentLog();
                            agentLog.setTraceList(messages);
                            TransmitSupport.getInstance().send(agentLog);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static TraceSupport getDefault() {
        return traceSupport;
    }

    public void send(Trace trace) {
        //注入应用名
        trace.setApplicationName(ConfigLoad.getDefault().getApplicationName());
        trace.setAddress(ConfigLoad.getDefault().get(ConfigConstants.ADDRESS));
        this.cache.offer(new Index(MemoryPool.get().allocateString(JsonUtil.toString(trace))));
    }

    @Getter
    private class Index implements Serializable {

        private List<Integer> indices;

        public Index(List<Integer> indices) {
            this.indices = indices;
        }
    }
}
