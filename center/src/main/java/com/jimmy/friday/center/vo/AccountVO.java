package com.jimmy.friday.center.vo;


import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * (GatewayAccount)表实体类
 *
 * @author makejava
 * @since 2023-12-08 14:17:16
 */
@Data
public class AccountVO implements Serializable {

    private Long id;

    private Integer lvl;

    private String title;

    private BigDecimal balance;
}

