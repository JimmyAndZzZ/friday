package com.jimmy.friday.center.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;


/**
 * (GatewayServiceWarn)表实体类
 *
 * @author makejava
 * @since 2024-04-16 14:05:52
 */
@Data
public class GatewayServiceWarn {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long serviceId;

    private Long providerId;

    private Date createDate;

    private String type;

    private String message;
}

