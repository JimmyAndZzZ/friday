package com.jimmy.friday.center.event;

import com.jimmy.friday.boot.core.gateway.Service;
import org.springframework.context.ApplicationEvent;

public class SuspectedFailEvent extends ApplicationEvent {

    public SuspectedFailEvent(Object source) {
        super(source);
    }

    private Service service;

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }
}
