package com.jimmy.friday.center.vo.gateway;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * (GatewayServiceConsumer)表实体类
 *
 * @author makejava
 * @since 2024-03-25 15:50:03
 */
@Data
public class ConsumerVO implements Serializable {

    private Long serviceId;

    private String ipAddress;

    private Date createDate;

    private String appId;

    private String clientName;

    private ProviderVO lastInvokeProvider;
}

