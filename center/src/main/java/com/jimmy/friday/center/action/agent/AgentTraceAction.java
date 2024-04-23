package com.jimmy.friday.center.action.agent;

import cn.hutool.core.collection.CollUtil;
import com.jimmy.friday.boot.core.agent.Trace;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.message.agent.AgentLog;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.service.HawkEyeLogService;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgentTraceAction implements Action<AgentLog> {

    @Autowired
    private HawkEyeLogService hawkEyeLogService;

    @Override
    public void action(AgentLog agentLog, ChannelHandlerContext channelHandlerContext) {
        List<Trace> traceList = agentLog.getTraceList();
        if (CollUtil.isEmpty(traceList)) {
            return;
        }

        hawkEyeLogService.push(traceList);
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.AGENT_LOG;
    }
}
