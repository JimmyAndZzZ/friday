package com.jimmy.friday.boot.message.agent;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

import java.util.Date;

@Data
public class AgentRunPoint implements Message {

    private Date date;

    private String className;

    private String methodName;

    private String applicationName;

    private String traceId;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.AGENT_POINT;
    }
}
