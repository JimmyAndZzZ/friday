package com.jimmy.friday.center.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * (GatewayRouteRule)表实体类
 *
 * @author makejava
 * @since 2023-12-14 18:11:02
 */
@Data
public class GatewayRouteRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long serviceId;

    private Long methodId;

    private String version;

    private String enabled;

    private String isForce;

    private Integer priority;

    private String consumerCondition;

    private String providerCondition;

}

