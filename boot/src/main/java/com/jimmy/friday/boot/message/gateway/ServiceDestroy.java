package com.jimmy.friday.boot.message.gateway;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

import java.util.List;

@Data
public class ServiceDestroy implements Message {

    private String id;

    private List<Service> services;

    public ServiceDestroy() {

    }

    public ServiceDestroy(String id, List<Service> services) {
        this.id = id;
        this.services = services;
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.SERVICE_DESTROY;
    }
}
