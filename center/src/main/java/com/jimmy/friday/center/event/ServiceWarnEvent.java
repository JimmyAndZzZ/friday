package com.jimmy.friday.center.event;

import com.jimmy.friday.boot.enums.gateway.ServiceWarnTypeEnum;
import org.springframework.context.ApplicationEvent;

import java.util.Date;

public class ServiceWarnEvent extends ApplicationEvent {

    private Date createDate;

    private Long serviceId;

    private Long providerId;

    private ServiceWarnTypeEnum serviceWarnType;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public ServiceWarnEvent(Object source) {
        super(source);
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public ServiceWarnTypeEnum getServiceWarnType() {
        return serviceWarnType;
    }

    public void setServiceWarnType(ServiceWarnTypeEnum serviceWarnType) {
        this.serviceWarnType = serviceWarnType;
    }
}
