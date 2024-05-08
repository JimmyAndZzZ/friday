package com.jimmy.friday.center.event;

import com.jimmy.friday.boot.enums.gateway.NotifyTypeEnum;
import com.jimmy.friday.boot.enums.gateway.ServiceTypeEnum;
import org.springframework.context.ApplicationEvent;

public class NotifyEvent extends ApplicationEvent {

    public NotifyEvent(Object source) {
        super(source);
    }

    private Long traceId;

    private String serviceId;

    private String errorMessage;

    private NotifyTypeEnum notifyType;

    private Integer progressRate;

    private Object response;

    private ServiceTypeEnum serviceTypeEnum;

    public ServiceTypeEnum getServiceTypeEnum() {
        return serviceTypeEnum;
    }

    public void setServiceTypeEnum(ServiceTypeEnum serviceTypeEnum) {
        this.serviceTypeEnum = serviceTypeEnum;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Long getTraceId() {
        return traceId;
    }

    public void setTraceId(Long traceId) {
        this.traceId = traceId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public NotifyTypeEnum getNotifyType() {
        return notifyType;
    }

    public void setNotifyType(NotifyTypeEnum notifyType) {
        this.notifyType = notifyType;
    }

    public Integer getProgressRate() {
        return progressRate;
    }

    public void setProgressRate(Integer progressRate) {
        this.progressRate = progressRate;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }
}