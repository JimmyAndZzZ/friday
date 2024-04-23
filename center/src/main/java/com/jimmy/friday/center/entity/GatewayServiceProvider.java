package com.jimmy.friday.center.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * (GatewayServiceProvider)表实体类
 *
 * @author makejava
 * @since 2023-12-08 14:17:23
 */
@Data
public class GatewayServiceProvider {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long serviceId;

    private Integer port;

    private Integer weight;

    private String status;

    private String ipAddress;

    private String isManual;

    private Date createDate;
}

