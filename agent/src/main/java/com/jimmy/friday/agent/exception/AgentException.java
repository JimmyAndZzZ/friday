package com.jimmy.friday.agent.exception;

public class AgentException extends RuntimeException {

    /**
     * 自己的日常 打log 用
     */
    protected String message;

    public AgentException(String desc, Exception e) {
        super(e);
        this.message = desc;
    }

    public AgentException(String desc) {
        super(desc);
        this.message = desc;
    }

    public AgentException() {
        super();
    }

    public AgentException(Throwable cause) {
        super(cause);
    }
}

