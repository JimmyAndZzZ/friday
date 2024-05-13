package com.jimmy.friday.center.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * (GatewayAccount)表实体类
 *
 * @author makejava
 * @since 2023-12-08 14:17:16
 */
@Data
public class GatewayAccount {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String appId;

    private String appSecret;

    private Date createDate;

    private Integer lvl;

    private String title;

    private String status;

    private BigDecimal balance;
}

