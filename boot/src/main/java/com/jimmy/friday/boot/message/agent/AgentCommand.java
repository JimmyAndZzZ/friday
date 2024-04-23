package com.jimmy.friday.boot.message.agent;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

import java.util.List;

@Data
public class AgentCommand implements Message {

    private Long traceId;

    private String content;

    private String command;

    private Boolean isSuccess = true;

    private String errorMessage;

    private List<String> param;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.AGENT_COMMAND;
    }
}
