package com.jimmy.friday.center.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class GatewayInvokeTrace {

    @TableId(type = IdType.INPUT)
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

}
