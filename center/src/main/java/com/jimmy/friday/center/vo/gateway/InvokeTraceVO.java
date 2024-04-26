package com.jimmy.friday.center.vo.gateway;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class InvokeTraceVO {

    private Long id;

    private Long providerId;

    private Long serviceId;

    private Long methodId;

    private String invokeParam;

    private String invokeResult;

    private Date createTime;

    private String errorMessage;

    private Long costTime;

    private String isSuccess;

    private String clientIpAddress;

    private String clientName;

    private BigDecimal cost;

    private String appId;

    private String checkStatus;
}
