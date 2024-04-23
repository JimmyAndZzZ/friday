package com.jimmy.friday.agent.support;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.primitives.Ints;
import com.jimmy.friday.agent.core.ConfigLoad;
import com.jimmy.friday.boot.core.agent.Qps;
import com.jimmy.friday.boot.message.agent.AgentQps;
import com.jimmy.friday.boot.other.ConfigConstants;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QpsSupport {

    private static QpsSupport qpsSupport;

    private List<Qps> qpsList = Lists.newArrayList();

    private ConcurrentLinkedQueue<Qps> cache = Queues.newConcurrentLinkedQueue();

    public static void build() {
        qpsSupport = new QpsSupport();
    }

    private QpsSupport() {
        new Thread(() -> {
            String batch = ConfigLoad.getDefault().get(ConfigConstants.BATCH_SIZE);
            int i = Strings.isNullOrEmpty(batch) ? 50 : Ints.tryParse(batch);

            while (true) {
                try {
                    Thread.sleep(100);

                    Qps poll = cache.poll();
                    if (poll != null) {
                        qpsList.add(poll);

                        if (qpsList.size() >= i) {
                            List<Qps> messages = Lists.newArrayList(qpsList);
                            qpsList.clear();

                            AgentQps agentQps = new AgentQps();
                            agentQps.setQpsList(messages);
                            agentQps.setServer(ConfigLoad.getDefault().getTopology());
                            TransmitSupport.getInstance().send(agentQps);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static QpsSupport getDefault() {
        return qpsSupport;
    }

    public void send(String requestPoint, Date createDate, String requestAttachment, String protocol) {
        Qps qps = new Qps();
        qps.setRequestAttachment(requestAttachment);
        qps.setCreateDate(createDate == null ? new Date() : createDate);
        qps.setRequestPoint(requestPoint);
        qps.setProtocol(protocol);
        this.cache.offer(qps);
    }
}
