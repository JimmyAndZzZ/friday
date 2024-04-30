package com.jimmy.friday.center.event;

import org.springframework.context.ApplicationEvent;

public class AckEvent extends ApplicationEvent {

    private String id;

    public AckEvent(Object source) {
        super(source);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
