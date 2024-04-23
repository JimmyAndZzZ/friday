package com.jimmy.friday.center.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;


/**
 * (GatewayServiceMethodOpen)表实体类
 *
 * @author makejava
 * @since 2024-01-05 17:20:20
 */
@Data
public class GatewayServiceMethodOpen {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long serviceId;

    private Long methodId;

    private String name;

    private String description;

    private String example;

    private String type;

    private String code;

    private Long costStrategyId;

    private String status;

    private String isFree;
}

