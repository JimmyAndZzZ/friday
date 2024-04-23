package com.jimmy.friday.boot.message.gateway;

import com.jimmy.friday.boot.base.Message;
import com.jimmy.friday.boot.core.gateway.InvokeParam;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GatewayInvoke implements Message {

    private Long traceId;

    private String applicationId;

    private String invokeMethod;

    private String invokeInterface;

    private String returnClass;

    private String jsonResult;

    private String error;

    private Boolean isSuccess = true;

    private Boolean isNeedRetry = false;

    private String exceptionClass;

    private Boolean isFallback = false;

    private List<InvokeParam> invokeParams = new ArrayList<>();

    public void addInvokeParam(String name, String className, String jsonData) {
        this.invokeParams.add(new InvokeParam(name, className, jsonData));
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.GATEWAY_INVOKE;
    }
}
