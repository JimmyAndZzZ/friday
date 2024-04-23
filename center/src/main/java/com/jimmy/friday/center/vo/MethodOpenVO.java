package com.jimmy.friday.center.vo;


import lombok.Data;

import java.io.Serializable;


/**
 * (GatewayServiceMethodOpen)表实体类
 *
 * @author makejava
 * @since 2024-01-05 17:20:20
 */
@Data
public class MethodOpenVO implements Serializable {

    private Long id;

    private Long methodId;

    private String name;

    private String description;

    private String example;

    private String type;

    private String code;

    private Long costStrategyId;

    private String costStrategyName;

    private String status;

    private String isFree;
}

