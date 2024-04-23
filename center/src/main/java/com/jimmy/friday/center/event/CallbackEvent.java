package com.jimmy.friday.center.event;

import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.CallbackTypeEnum;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import org.springframework.context.ApplicationEvent;

public class CallbackEvent extends ApplicationEvent {

    public CallbackEvent(Object source) {
        super(source);
    }

    private Service service;

    private ServiceTypeEnum serviceTypeEnum;

    private CallbackTypeEnum callbackTypeEnum;

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public ServiceTypeEnum getServiceTypeEnum() {
        return serviceTypeEnum;
    }

    public void setServiceTypeEnum(ServiceTypeEnum serviceTypeEnum) {
        this.serviceTypeEnum = serviceTypeEnum;
    }

    public CallbackTypeEnum getCallbackTypeEnum() {
        return callbackTypeEnum;
    }

    public void setCallbackTypeEnum(CallbackTypeEnum callbackTypeEnum) {
        this.callbackTypeEnum = callbackTypeEnum;
    }
}
