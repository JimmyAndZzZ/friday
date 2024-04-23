package com.jimmy.friday.center.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;


/**
 * (GatewayServiceConsumer)表实体类
 *
 * @author makejava
 * @since 2024-03-25 15:50:03
 */
@Data
public class GatewayServiceConsumer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long serviceId;

    private String ipAddress;

    private Date createDate;

    private String appId;

    private String clientName;

    private Long providerId;
}

