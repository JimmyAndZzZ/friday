package com.jimmy.friday.center.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


/**
 * (GatewayServiceMethod)表实体类
 *
 * @author makejava
 * @since 2024-01-02 16:10:08
 */
@Data
public class GatewayServiceMethod {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long serviceId;

    private String name;

    private String interfaceName;

    private String returnType;

    private String paramType;

    private Integer retry;

    private Integer timeout;

    private String methodId;

    private String isManual;

    private String methodCode;

}

