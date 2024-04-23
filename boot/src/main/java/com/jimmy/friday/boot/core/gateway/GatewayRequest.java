package com.jimmy.friday.boot.core.gateway;

import com.jimmy.friday.boot.other.GlobalConstants;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class GatewayRequest implements Serializable {

    private Long id;

    private String methodName;

    private String serviceName;

    private String version = GlobalConstants.DEFAULT_VERSION;

    private String invokeInterface;

    private String serviceType;

    private Integer retry = 0;

    private String clientIpAddress;

    private String clientName;

    private String methodId;

    private String methodCode;

    private String appId;

    private BigDecimal cost;

    private String applicationId;

    private Boolean isDebug = false;

    private Boolean isApi = false;

    private Integer timeout = GlobalConstants.DEFAULT_TIMEOUT;

    private Map<String, String> tag = new HashMap<>();

    private List<InvokeParam> invokeParams = new ArrayList<>();

    private byte[] attachment;

    public void addInvokeParam(String name, String className, String jsonData) {
        this.invokeParams.add(new InvokeParam(name, className, jsonData));
    }

    public void addExtraParams(String name, String className, String jsonData) {
        this.invokeParams.add(new InvokeParam(name, className, jsonData));
    }

    public void addTag(String name, String value) {
        this.tag.put(name, value);
    }

    public void setTimeout(Integer timeout) {
        if (timeout != null && timeout > 0) {
            this.timeout = timeout;
        }
    }
}
