package com.jimmy.friday.boot.message.gateway;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

import java.util.List;

@Data
public class ServiceReload implements Message {

    private String id;

    private List<Service> services;

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.SERVICE_RELOAD;
    }
}
