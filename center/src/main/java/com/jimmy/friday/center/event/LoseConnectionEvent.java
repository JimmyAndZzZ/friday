package com.jimmy.friday.center.event;

import org.springframework.context.ApplicationEvent;

public class LoseConnectionEvent extends ApplicationEvent {

    private String name;

    private String ip;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public LoseConnectionEvent(Object source) {
        super(source);
    }
}
