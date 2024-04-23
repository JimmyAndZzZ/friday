package com.jimmy.friday.agent.support;

import com.jimmy.friday.agent.core.ConfigLoad;
import com.jimmy.friday.boot.message.agent.AgentRunPoint;

import java.util.Date;

public class PointSupport {

    private static class SingletonHolder {
        private static final PointSupport INSTANCE = new PointSupport();
    }

    private PointSupport() {

    }

    public static PointSupport getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void send(Date date,
                     String className,
                     String methodName,
                     String traceId) {
        AgentRunPoint agentRunPoint = new AgentRunPoint();
        agentRunPoint.setDate(date);
        agentRunPoint.setClassName(className);
        agentRunPoint.setMethodName(methodName);
        agentRunPoint.setApplicationName(ConfigLoad.getDefault().getApplicationName());
        agentRunPoint.setTraceId(traceId);
        TransmitSupport.getInstance().send(agentRunPoint);
    }
}
