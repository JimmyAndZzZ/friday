package com.jimmy.friday.center.vo;


import lombok.Data;

import java.io.Serializable;


/**
 * (GatewayServiceMethodParam)表实体类
 *
 * @author makejava
 * @since 2024-03-26 17:55:21
 */
@Data
public class MethodParamVO implements Serializable {

    private Long id;

    private String name;

    private String desc;

    private String paramType;
}

