package com.jimmy.friday.center.vo.gateway;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


/**
 * (GatewayAccountOrder)表实体类
 *
 * @author makejava
 * @since 2024-01-09 14:22:05
 */
@Data
public class OrderVO {

    private Long id;

    private Long orderTraceNo;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

    private BigDecimal amount;

    private String source;

    private String trdOrderTraceNo;

    private String purpose;
}

